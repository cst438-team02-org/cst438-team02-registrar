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

        WebElement row = driver.findElement(By.xpath("//tr[./td[text()='cst489']]"));
        assertNotNull(row);
        row.findElement(By.id("assignmentsLink")).click();
        driver.findElement(By.id("addAssignmentButton")).click();
        Thread.sleep(DELAY);
        driver.findElement(By.id("title")).sendKeys("Assignments");
        driver.findElement(By.id("duedate")).sendKeys("02-15-2026");
        driver.findElement(By.id("save")).click();
        driver.findElement(By.id("close")).click();
    }
}
