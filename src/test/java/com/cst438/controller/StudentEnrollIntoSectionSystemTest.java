package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

public class StudentEnrollIntoSectionSystemTest {

    
    // CONFIGURATION
    
    public static final String CHROME_DRIVER_FILE_LOCATION = "chromedriver";

    public static final String URL = "http://localhost:3000/";
    public static final String STUDENT_EMAIL = "sama@csumb.edu";
    public static final String STUDENT_PASSWORD = "sama2025";  

    private WebDriver driver;
    private WebDriverWait wait;

    
    // SETUP DRIVER
    
    @BeforeEach
    public void setUpDriver() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);

        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(ops);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(4));

        driver.get(URL);
        driver.manage().window().maximize();
    }


    // CLEANUP
    
    @AfterEach
    public void quit() {
        driver.quit();
    }

    
    // MAIN SYSTEM TEST: Student enrolls into CST599 (Spring 2026)
    
    @Test
    public void testStudentEnrollIntoSection() throws Exception {

    
        
        // 1. LOGIN AS STUDENT (SAMA)
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys(STUDENT_EMAIL);
        driver.findElement(By.id("password")).sendKeys(STUDENT_PASSWORD);
        driver.findElement(By.id("loginButton")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Student Home')]")
        ));

      
        // 2. VIEW SCHEDULE (Spring 2026)
        
        driver.findElement(By.id("scheduleLink")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));
        driver.findElement(By.id("year")).clear();
        driver.findElement(By.id("year")).sendKeys("2026");

        driver.findElement(By.id("semester")).sendKeys("Spring");

        driver.findElement(By.id("selectTermButton")).click();

        // Wait for schedule table
        Thread.sleep(1000);

        
        // 3. DROP EXISTING ENROLLMENT FOR CST599 (if exists)
        
        List<WebElement> dropButtons = driver.findElements(
            By.xpath("//tr[td[text()='CST599']]//button[contains(text(),'Drop')]")
        );

        if (!dropButtons.isEmpty()) {
            dropButtons.get(0).click();

            // Handle Confirm Alert
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
            ));
            WebElement yesButton = driver.findElement(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
            );
            yesButton.click();

            Thread.sleep(1200); // allow UI refresh
        }

        
        
        // 4. NAVIGATE TO ENROLL PAGE
   
        driver.findElement(By.id("addCourseLink")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h3[contains(text(),'Course Enrollment')]")
        ));

       
        // 5. SELECT CST599 AND ENROLL
       
        WebElement enrollBtn = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[text()='CST599']]//button[contains(text(),'Enroll')]")
            )
        );

        enrollBtn.click();

        // Confirm Alert
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
        ));
        WebElement yes = driver.findElement(
            By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
        );
        yes.click();

        Thread.sleep(1500);

      
        // 6. VERIFY TRANSCRIPT → CST599 EXISTS WITH BLANK GRADE
        
        driver.findElement(By.id("transcriptLink")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));

        WebElement row = driver.findElement(
            By.xpath("//tr[td[text()='CST599']]")
        );

        WebElement gradeCell = row.findElement(By.xpath("./td[last()]"));
        assertTrue(gradeCell.getText().isBlank(), "Expected grade to be blank after enrolling.");

     
        // 7. LOGOUT STUDENT
      
        driver.findElement(By.id("logoutLink")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

       
        // 8. LOGIN AS INSTRUCTOR (Ted)
       
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Instructor Home')]")
        ));

      
        // 9. ENTER TERM (Spring 2026)
       
        driver.findElement(By.id("year")).clear();
        driver.findElement(By.id("year")).sendKeys("2026");

        driver.findElement(By.id("semester")).sendKeys("Spring");

        driver.findElement(By.id("selectTermButton")).click();

       
        // 10. FIND CST599 IN SECTIONS LIST
        
        WebElement cst599row = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[text()='CST599']]")
            )
        );

        
        // 11. OPEN ENROLLMENTS VIEW
      
        cst599row.findElement(By.id("enrollmentsLink")).click();

        Thread.sleep(1000);

       
        // 12. VERIFY SAMA APPEARS ONLY ONCE IN THE ROSTER
       
        List<WebElement> occurrences = driver.findElements(
            By.xpath("//tr[td[text()='sama@csumb.edu']]")
        );

        assertEquals(1, occurrences.size(), "SAMA should appear exactly once in the roster.");
    }
}
