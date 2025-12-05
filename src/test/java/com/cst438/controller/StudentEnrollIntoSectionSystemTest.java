package com.cst438;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

public class StudentEnrollIntoSectionSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION = "chromedriver";
    public static final String URL = "http://localhost:3000/";
    public static final String TEST_USER_EMAIL = "sama@csumb.edu";
    public static final String TEST_USER_PASSWORD = "test123"; // matches data.sql

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.get(URL);
        driver.manage().window().maximize();
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void studentEnrollSystemTest() throws Exception {

        
        // LOGIN AS STUDENT (sama)
        
        wait.until(visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys(TEST_USER_EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_USER_PASSWORD);
        driver.findElement(By.id("loginButton")).click();

        wait.until(visibilityOfElementLocated(
            By.xpath("//h3[contains(text(),'Student Home')]")
        ));

        
        // VIEW SCHEDULE FOR FALL 2025
    
        driver.findElement(By.id("scheduleLink")).click();
        wait.until(visibilityOfElementLocated(By.id("year")));
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("searchButton")).click();

        wait.until(visibilityOfElementLocated(By.tagName("table")));

        
        // DROP CST599 IF ENROLLED
        
        List<WebElement> dropButtons =
            driver.findElements(By.xpath("//button[contains(text(),'Drop')]"));

        if (!dropButtons.isEmpty()) {
            dropButtons.get(0).click();

            // Confirm React ConfirmAlert
            wait.until(visibilityOfElementLocated(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
            ));
            WebElement yesButton = driver.findElement(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
            );
            yesButton.click();

            Thread.sleep(1000);
        }

        
        // NAVIGATE TO ENROLL PAGE
        
        driver.findElement(By.id("enrollLink")).click();
        wait.until(visibilityOfElementLocated(By.tagName("table")));

        
        // SELECT CST599 AND ENROLL
        
        WebElement enrollButton = driver.findElement(
            By.xpath("//tr[td[contains(text(),'CST599')]]//button[contains(text(),'Enroll')]")
        );
        enrollButton.click();

        wait.until(visibilityOfElementLocated(
            By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
        ));
        WebElement yesButton2 = driver.findElement(
            By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")
        );
        yesButton2.click();

        Thread.sleep(1000);

        
        // VIEW TRANSCRIPT → VERIFY CST599 IS LISTED WITH NO GRADE
        
        driver.findElement(By.id("transcriptLink")).click();
        wait.until(visibilityOfElementLocated(By.tagName("table")));

        WebElement courseRow = driver.findElement(
            By.xpath("//tr[td[contains(text(),'CST599')]]")
        );
        WebElement gradeCell = courseRow.findElement(By.xpath("./td[last()]"));

        assertTrue(gradeCell.getText().isBlank(), "Grade should be blank after enrollment.");

    
        // LOGIN AS INSTRUCTOR (ted)
        
        driver.findElement(By.id("logoutButton")).click();

        wait.until(visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        wait.until(visibilityOfElementLocated(
            By.xpath("//h3[contains(text(),'Instructor Home')]")
        ));

        
        // VIEW SECTIONS FOR FALL 2025
        
        driver.findElement(By.id("sectionSearchLink")).click();
        wait.until(visibilityOfElementLocated(By.id("year")));
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("searchButton")).click();

        wait.until(visibilityOfElementLocated(
            By.xpath("//tr[td[contains(text(),'CST599')]]")
        ));

        
        // VIEW ENROLLMENTS + VERIFY SAMA APPEARS ONLY ONCE
        
        driver.findElement(
            By.xpath("//tr[td[contains(text(),'CST599')]]//button[contains(text(),'Enrollments')]")
        ).click();

        wait.until(visibilityOfElementLocated(By.tagName("table")));

        List<WebElement> samaRows = driver.findElements(
            By.xpath("//tr[td[contains(text(),'sama')]]")
        );

        assertEquals(1, samaRows.size(), "sama should appear exactly once in the roster.");
    }
}
