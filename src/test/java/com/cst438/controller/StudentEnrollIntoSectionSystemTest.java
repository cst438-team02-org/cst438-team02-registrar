package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class StudentEnrollIntoSectionSystemTest {

  
    // CONFIGURATION
 
    public static final String CHROME_DRIVER_FILE_LOCATION = "chromedriver";
    public static final String URL = "http://localhost:5173/";  

    // Student credentials
    public static final String STUDENT_EMAIL = "sama@csumb.edu";
    public static final String STUDENT_PASSWORD = "sama2025";

    private WebDriver driver;
    private WebDriverWait wait;


    // SETUP

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


    @Test
    public void testStudentEnrollIntoSection() throws InterruptedException {

        // 1. LOGIN AS STUDENT (sama)

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys(STUDENT_EMAIL);
        driver.findElement(By.id("password")).sendKeys(STUDENT_PASSWORD);
        driver.findElement(By.id("loginButton")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Student Home')]")
        ));


        

        // 2. VIEW SCHEDULE FOR SPRING 2026

        driver.findElement(By.id("scheduleLink")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));
        driver.findElement(By.id("year")).sendKeys("2026");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("selectTermButton")).click();

        Thread.sleep(800);


  
        // 3. DROP CST599 IF ALREADY ENROLLED

        // Find any table row that contains CST599
        List<WebElement> existingRows = driver.findElements(
            By.xpath("//tr[td[text()='CST599']]")
        );

        if (!existingRows.isEmpty()) {
            WebElement courseRow = existingRows.get(0);

            WebElement dropButton = courseRow.findElement(
                By.xpath(".//button[contains(text(),'Drop')]")
            );

            dropButton.click();

            // handle confirmAlert
            WebElement yesButton = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
                )
            );
            yesButton.click();
            Thread.sleep(1200);
        }



        // 4. NAVIGATE TO ENROLL PAGE

        driver.findElement(By.id("addCourseLink")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h3[contains(text(),'Open Sections Available for Enrollment')]")
        ));



        // 5. SELECT CST599 AND ADD COURSE

        WebElement cst599Row = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[text()='CST599']]")
            )
        );

        WebElement addCourseButton = cst599Row.findElement(
            By.xpath(".//button[contains(text(),'Add Course')]")
        );

        addCourseButton.click();

        // Confirm "Yes"
        WebElement yesEnroll = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
            )
        );
        yesEnroll.click();

        Thread.sleep(1300);


        // 6. VIEW TRANSCRIPT AND VERIFY NO GRADE

        driver.findElement(By.id("transcriptLink")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));

        WebElement transcriptRow = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[text()='CST599']]")
            )
        );

        WebElement gradeCell = transcriptRow.findElement(By.xpath("./td[last()]"));
        assertTrue(gradeCell.getText().isBlank(), "Grade should be blank after enrollment.");


   
        // 7. LOGOUT STUDENT

        driver.findElement(By.id("logoutLink")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));


    
        // 8. LOGIN AS INSTRUCTOR TED

        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Instructor Home')]")
        ));


        // 9. GET SECTIONS FOR SPRING 2026
 
        driver.findElement(By.id("year")).sendKeys("2026");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("selectTermButton")).click();

        WebElement rowAfter = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[text()='CST599']]")
            )
        );


        
 
        // 10. OPEN ENROLLMENTS FOR CST599
 
        rowAfter.findElement(By.id("enrollmentsLink")).click();

        Thread.sleep(800);


    
        // 11. VERIFY SAMA APPEARS ONLY ONCE

        List<WebElement> samaRows = driver.findElements(
            By.xpath("//tr[td[text()='sama@csumb.edu']]")
        );

        assertEquals(1, samaRows.size(), "Sama should appear once in instructor roster.");
    }
}
