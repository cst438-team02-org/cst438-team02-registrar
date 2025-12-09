package com.cst438.controller;

import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstructorCreatesAssignmentSystemTest {
    static final String CHROME_DRIVER_FILE_LOCATION = "/Users/matthewross/Downloads/chromedriver-mac-x64M/chromedriver";
    static final String URL = "http://localhost:5173";   // react dev server

    static final int DELAY = 2000;
    WebDriver driver;

    Wait<WebDriver> wait;

    Random random = new Random();

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
    @Test
    public void testInstructorCreatesAssignment() throws Exception {
        String email = "ted@csumb.edu";
        String password = "ted2025";

        // login
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginButton")).click();

        // switch to assignment screen
        driver.findElement(By.id("year")).sendKeys("2026");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("selectTermButton")).click();

        WebElement row = driver.findElement(By.xpath("//tr[./td[text()='cst599']]"));
        assertNotNull(row);
        row.findElement(By.id("assignmentsLink")).click();
        driver.findElement(By.id("addAssignmentButton")).click();
        String assignment_title = "assignment"+random.nextInt(1,1000000);
        String assignment_dueDate = "02152026";
        String xpath="//tr[./td[text()='"+assignment_title+"']]";
        Thread.sleep(DELAY);
        driver.findElement(By.id("titleAddAssignment")).sendKeys(assignment_title);
        driver.findElement(By.id("duedateAddAssignment")).sendKeys(assignment_dueDate);
        driver.findElement(By.id("saveAddAssignment")).click();
        Thread.sleep(DELAY);
        driver.findElement(By.id("closeAddAssignment")).click();
        WebElement NewAssignment= driver.findElement(By.xpath(xpath));
        assertNotNull(NewAssignment);
        NewAssignment.findElement(By.id("gradeButton")).click();
        Thread.sleep(DELAY);
        WebElement row2 = driver.findElement(By.xpath("//tr[./td[text()='sama']]"));
        WebElement row3 = driver.findElement(By.xpath("//tr[./td[text()='samb']]"));
        WebElement row4= driver.findElement(By.xpath("//tr[./td[text()='samc']]"));
        String[] grades={"60","88","98"};
        WebElement[] rows= {row2,row3,row4};
        int i =0;
        for(WebElement r: rows){
            r.findElement(By.id("score"+assignment_title)).sendKeys(grades[i]);
            i++;
        }
        Thread.sleep(DELAY);
        driver.findElement(By.id("saveGrades"+assignment_title)).click();
        driver.findElement(By.id("closeGrades"+assignment_title)).click();
        NewAssignment.findElement(By.id("gradeButton")).click();
        row2 = driver.findElement(By.xpath("//tr[./td[text()='sama']]"));
        row3 = driver.findElement(By.xpath("//tr[./td[text()='samb']]"));
        row4= driver.findElement(By.xpath("//tr[./td[text()='samc']]"));
        rows= new WebElement[]{row2, row3, row4};
        grades=new String[]{String.valueOf(random.nextInt(100)),String.valueOf(random.nextInt(100)),String.valueOf(random.nextInt(100))};
        i=0;
        for(WebElement r: rows){
            r.findElement(By.id("score"+assignment_title)).clear();
            r.findElement(By.id("score"+assignment_title)).sendKeys(grades[i]);
            i++;
        }
        i=0;
        for (WebElement r : rows) {
            WebElement input = r.findElement(By.id("score"+assignment_title));

            String value = input.getAttribute("value");

            assertEquals(grades[i], value);
            i++;
        }
        driver.findElement(By.id("closeGrades"+assignment_title)).click();


    }
}
