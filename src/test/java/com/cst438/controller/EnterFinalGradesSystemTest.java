package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class EnterFinalGradesSystemTest {
    static final String CHROME_DRIVER_FILE_LOCATION = "C:/Home/School Stuff/CST 438/Labs/Lab4/chromedriver-win64/chromedriver.exe";
    static final String URL = "http://localhost:5173";   // react dev server

    WebDriver driver;

    Wait<WebDriver> wait;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        driver.get(URL);
    }

    @AfterEach
    public void quit() {
        driver.quit();
    }

    /* System Test to Test Instructor Entering Final Grades Successfully */
    @Test
    public void enterFinalGrades() throws InterruptedException {
        /* Instructor ted@csumb.edu logins */
        // wait until login page appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

        // find email & password fields, then enter data
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        /* On the home page for instructor enter 2026 and Spring to view the list of sections */
        // wait until instructor homepage & term field appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Instructor Home']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));

        // find term year & semester fields, then enter data
        driver.findElement(By.id("year")).sendKeys("2026");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("selectTermButton")).click();

        /* Click on enrollments for CST599 */
        // wait for list of sections to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='cst599']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enrollmentsLink")));

        // find the row for CST599
        WebElement courseElement = driver.findElement(By.xpath("//tr[td[text()='cst599']]"));

        // navigate to the enrollments view for CST599
        courseElement.findElement(By.id("enrollmentsLink")).click();

        /* View the class roster */
        // wait for class roster to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='sama']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='samb']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='samc']]")));

        /* Clear any existing grade and enter final grades of  A, B+ and C for the sama, samb and samc */
        // grade sama
        // find sama's web element
        WebElement samaElement = driver.findElement(By.xpath("//tr[td[text()='sama']]"));
        // find sama's grade field
        WebElement samaGrade = samaElement.findElement(By.xpath(".//input"));
        // clear sama's grade field
        samaGrade.clear();
        // enter sama's final grades
        samaGrade.sendKeys("A");

        // grade samb
        WebElement sambElement = driver.findElement(By.xpath("//tr[td[text()='samb']]"));
        // find samb's grade field
        WebElement sambGrade = sambElement.findElement(By.xpath(".//input"));
        // clear samb's grade field
        sambGrade.clear();
        // enter samb's final grades
        sambGrade.sendKeys("B+");

        // grade samc
        WebElement samcElement = driver.findElement(By.xpath("//tr[td[text()='samc']]"));
        // find samc's grade field
        WebElement samcGrade = samcElement.findElement(By.xpath(".//input"));
        // clear samc's grade field
        samcGrade.clear();
        // enter samc's final grades
        samcGrade.sendKeys("C");

        // save the grades
        driver.findElement(By.id("saveGrades")).click();

        // verify response message
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[li[text()='Grades updated successfully.']]")));
        WebElement response = driver.findElement(By.xpath("//ul[li[text()='Grades updated successfully.']]"));
        assertNotNull(response, "success message didn't appear");

        /* View the class roster again and verify the grades */
        // navigate to homepage
        driver.findElement(By.id("homeLink")).click();

        // wait until instructor homepage & term field appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Instructor Home']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));

        // find term year & semester fields, then enter data
        driver.findElement(By.id("year")).sendKeys("2026");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("selectTermButton")).click();

        // wait for list of sections to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='cst599']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enrollmentsLink")));

        // click on enrollments for CST599
        courseElement = driver.findElement(By.xpath("//tr[td[text()='cst599']]"));

        // navigate to the enrollments view for CST599
        courseElement.findElement(By.id("enrollmentsLink")).click();

        // wait for class roster to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='sama']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='samb']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='samc']]")));

        // find each student's web element
        samaElement = driver.findElement(By.xpath("//tr[td[text()='sama']]"));
        sambElement = driver.findElement(By.xpath("//tr[td[text()='samb']]"));
        samcElement = driver.findElement(By.xpath("//tr[td[text()='samc']]"));

        // find each student's grade field
        samaGrade = samaElement.findElement(By.xpath(".//input[@value='A']"));
        sambGrade = sambElement.findElement(By.xpath(".//input[@value='B+']"));
        samcGrade = samcElement.findElement(By.xpath(".//input[@value='C']"));

        // verify student grades
        assertNotNull(samaGrade, "sama's grade is not expected");
        assertNotNull(sambGrade, "samb's grade is not expected");
        assertNotNull(samcGrade, "samc's grade is not expected");

        /* Logout of Instructor Account */
        driver.findElement(By.id("logoutLink")).click();

        // wait until login page appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

        /* Login as student samb */
        // find email & password fields, then enter data
        driver.findElement(By.id("email")).sendKeys("samb@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("samb2025");
        driver.findElement(By.id("loginButton")).click();

        // wait until student homepage appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Student Home']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transcriptLink")));

        /* Navigate to view transcript and verify that cst599 with a grade of B+ is listed */
        // navigate to transcript link
        driver.findElement(By.id("transcriptLink")).click();

        // wait until transcript page appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Transcript']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='cst599']]")));

        // verify samb has the grade B+ listed in their transcript for CST599
        WebElement cst599Element = driver.findElement(By.xpath("//tr[td[text()='cst599']]"));
        sambGrade = cst599Element.findElement(By.xpath("./td[text()='B+']"));
        assertNotNull(sambGrade, "samb's grade is not expected");
    }

    /* System Test to Test Instructor Entering Final Grades with Invalid Grades */
    @Test
    public void enterInvalidFinalGrades() throws InterruptedException {
        /* Instructor ted@csumb.edu logins */
        // wait until login page appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

        // find email & password fields, then enter data
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        /* On the home page for instructor enter 2026 and Spring to view the list of sections */
        // wait until instructor homepage & term field appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Instructor Home']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));

        // find term year & semester fields, then enter data
        driver.findElement(By.id("year")).sendKeys("2026");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("selectTermButton")).click();

        /* Click on enrollments for CST599 */
        // wait for list of sections to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='cst599']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enrollmentsLink")));

        // find the row for CST599
        WebElement courseElement = driver.findElement(By.xpath("//tr[td[text()='cst599']]"));

        // navigate to the enrollments view for CST599
        courseElement.findElement(By.id("enrollmentsLink")).click();


        /* View the class roster */
        // wait for class roster to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='sama']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='samb']]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td[text()='samc']]")));

        /* Clear any existing grade and enter final grades over the size limit for the sama, samb and samc */
        // grade sama
        // find sama's web element
        WebElement samaElement = driver.findElement(By.xpath("//tr[td[text()='sama']]"));
        // find sama's grade field
        WebElement samaGrade = samaElement.findElement(By.xpath(".//input"));
        // clear sama's grade field
        samaGrade.clear();
        // enter sama's final grades
        samaGrade.sendKeys("AAAAAA");

        // grade samb
        WebElement sambElement = driver.findElement(By.xpath("//tr[td[text()='samb']]"));
        // find samb's grade field
        WebElement sambGrade = sambElement.findElement(By.xpath(".//input"));
        // clear samb's grade field
        sambGrade.clear();
        // enter samb's final grades
        sambGrade.sendKeys("B+B+B+B+B+B+BBBB");

        // grade samc
        WebElement samcElement = driver.findElement(By.xpath("//tr[td[text()='samc']]"));
        // find samc's grade field
        WebElement samcGrade = samcElement.findElement(By.xpath(".//input"));
        // clear samc's grade field
        samcGrade.clear();
        // enter samc's final grades
        samcGrade.sendKeys("CCCCCCCCCCC");

        // save the grades
        driver.findElement(By.id("saveGrades")).click();

        // verify response message
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[li[text()='Error updating grades.']]")));
        WebElement response = driver.findElement(By.xpath("//ul[li[text()='Error updating grades.']]"));
        assertNotNull(response, "error message didn't appear");
    }
}

