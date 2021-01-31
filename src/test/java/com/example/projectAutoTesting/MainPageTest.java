package com.example.projectAutoTesting;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;

import java.util.Random;

/**
 * Poziadavky zakaznika:
 * - amount, cislo v intervale <0;1000000>
 * - interest, cislo v intervale <0;100>
 * - period , pocet rokov: 1,2,3,4,5
 * uzivatel musim vyplnit vsetky vstupne polia, zaroven musi zaskrtnut suhlas so spracovanim udajov
 * - uzivatel ma na vyber, ci vynosy su zdanene ( prednastavena hodnota: yes)
 * - kliknutim na reset sa formular vymaze
 * - kliknuti na vypocet sa najprv skontroluje, ci je vsetko zadane a v rozmedzi, ak ano, prevedie sa vypocet a vypise sa pod formularom.
 * - chybove hlasky su cervenou farbou.
 * Pokial uzivatel po vypocte zmeni nejaky parameter, vysledok sa zmaze a musi uzivatel znova kliknut na tlacidlo vypocet.
 */

public class MainPageTest {

    static final String URL = "http://itsovy.sk/testing";
    private static WebDriver webDriver;
    Random rnd = new Random();
    WebElement errorMessage, amount, interest, periodRng, periodLbl, taxYes, taxNo, agreement, resetBtn, calculateBtn, result;

    @BeforeAll
    public static void setUp() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void linkTestSettings() {
        webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
        webDriver.get(URL);

        errorMessage = webDriver.findElement(By.id("error"));
        amount = webDriver.findElement(By.id("amount"));
        interest = webDriver.findElement(By.id("interest"));
        periodRng = webDriver.findElement(By.id("period"));
        periodLbl = webDriver.findElement(By.id("lblPeriod"));
        taxYes = webDriver.findElement(By.cssSelector("input[value=\"y\"]"));
        taxNo = webDriver.findElement(By.cssSelector("input[value=\"n\"]"));
        agreement = webDriver.findElement(By.id("confirm"));
        resetBtn = webDriver.findElement(By.id("btnreset"));
        calculateBtn = webDriver.findElement(By.id("btnsubmit"));
        result = webDriver.findElement(By.id("result"));

    }

    @AfterAll
    public static void teardown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Test
    public void intervalCheckAmount() {
        interest.sendKeys(String.valueOf(4));
        agreement.click();
        for (int i = 0; i <= 1000000; i += 100) {
            amount.sendKeys(String.valueOf(i));
            calculateBtn.click();
            Assert.assertFalse(errorMessage.isDisplayed());
            amount.clear();
        }

        for (int i = 0; i <= 10; i++) {
            amount.sendKeys(String.valueOf(rnd.nextInt(1) + -10000001));
            calculateBtn.click();
            Assert.assertTrue(errorMessage.isDisplayed());
            amount.clear();
        }

        for (int i = 0; i <= 10; i++) {
            amount.sendKeys(String.valueOf(rnd.nextInt(1000000) + 100000000));
            calculateBtn.click();
            Assert.assertTrue(errorMessage.isDisplayed());
            amount.clear();
        }
    }

    @Test
    public void intervalCheckInterest() {
        amount.sendKeys(String.valueOf(4));
        agreement.click();
        for (int i = 0; i <= 100; i++) {
            interest.sendKeys(String.valueOf(i));
            calculateBtn.click();
            Assert.assertFalse(errorMessage.isDisplayed());
            interest.clear();
        }

        for (int i = 0; i <= 10; i++) {
            interest.sendKeys(String.valueOf(rnd.nextInt(1) + -10000001));
            calculateBtn.click();
            Assert.assertTrue(errorMessage.isDisplayed());
            interest.clear();
        }

        for (int i = 0; i <= 10; i++) {
            interest.sendKeys(String.valueOf(rnd.nextInt(100) + 100000000));
            calculateBtn.click();
            Assert.assertTrue(errorMessage.isDisplayed());
            interest.clear();
        }
    }

    @Test
    public void resultCheck() {
        amount.sendKeys(String.valueOf(20000));
        interest.sendKeys(String.valueOf(1));
        taxYes.click();
        agreement.click();

        interest.sendKeys(String.valueOf(5));
        for (int i = 0; i <= 4; i++) {
            periodRng.sendKeys(Keys.RIGHT);
        }

        calculateBtn.click();
        Assert.assertEquals(result.getText(), "Total amount : 35246.84 , net profit : 15246.84");
        Assert.assertNotEquals(result.getText(), "Total amount : 25246.84 , net profit : 15246.84");
    }

    @Test
    public void allFieldsAreFilledCheck() {
        amount.sendKeys("");
        interest.sendKeys("");
        periodRng.sendKeys("");
        taxYes.isDisplayed();
        taxNo.isDisplayed();
        agreement.isDisplayed();
        calculateBtn.click();
        Assert.assertTrue(errorMessage.isDisplayed());
    }

    @Test
    public void resetBtnCheck() {
        amount.sendKeys(String.valueOf(4));
        interest.sendKeys(String.valueOf(4));
        agreement.click();
        calculateBtn.click();
        resetBtn.click();
        Assert.assertFalse(errorMessage.isDisplayed());
        Assert.assertEquals(amount.getText(), "");
        Assert.assertEquals(interest.getText(), "");
        Assert.assertEquals(periodRng.getAttribute("value"), "1");
        Assert.assertTrue(taxYes.isSelected());
        Assert.assertFalse(taxNo.isSelected());
        Assert.assertFalse(agreement.isSelected());
        Assert.assertFalse(result.isDisplayed());
    }

    @Test
    public void resultDeleting() {
        amount.sendKeys(String.valueOf(4));
        interest.sendKeys(String.valueOf(4));
        agreement.click();
        calculateBtn.click();
        interest.sendKeys(String.valueOf(5));

        Assert.assertFalse(result.isDisplayed());
    }

    @Test
    public void errorColorTest() {
        resetBtn.click();
        calculateBtn.click();
        Assert.assertTrue(errorMessage.isDisplayed());
        Assert.assertEquals(errorMessage.getCssValue("color"), "rgba(255, 0, 0, 1)");

    }

    @Test
    public void intervalCheckPeriod() {
        for (int i = 0; i <= 10; i++) {
            periodRng.sendKeys(Keys.RIGHT);
        }
        for (int i = 0; i <= 10; i++) {
            periodRng.sendKeys(Keys.LEFT);
        }
    }

    @Test
    public void overMillionTest() {
        amount.sendKeys(String.valueOf(rnd.nextInt(9000000) + 1000000));
        interest.sendKeys(String.valueOf(rnd.nextInt(100)));
        agreement.click();
        calculateBtn.click();
        Assert.assertTrue(errorMessage.isDisplayed());
        Assert.assertEquals(errorMessage.getCssValue("color"), "rgba(255, 0, 0, 1)");
        Assert.assertEquals(errorMessage.getText(), new String("Amount must be a number between 0 and 1000000 !"));
        resetBtn.click();
    }
}

