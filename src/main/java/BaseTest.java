import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BaseTest {
    private final String BUSINESS_TRIP_LOCATION = "/businesstrip/list.do";
    private static final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private WebDriver driver;

    @Test(description = "Log in to CTC")
    @Parameters({"userName", "pwdName","baseUrl"})
    public void logIn(String userName, String pwdName, String baseUrl){
        String textForComparing = "Logged in as";
        driver.get(baseUrl + "/login.do?logout=true&tz=GMT%2B06:00");
        driver.findElement(By.xpath("//input[@name='username']"))
                .sendKeys(userName);
        driver.findElement(By.xpath("//input[@name='password']"))
                .sendKeys(pwdName);
        driver.findElement(By.xpath("//input[@name='Login']"))
                .click();
        String result = driver.findElement(By.xpath("//*[@id='headerLogin']/div[@class='blInfoLogin']"))
                .getText();
        Assert.assertTrue(result.contains(textForComparing),"Impossible to login to CTC");

    }

    @Test(dependsOnMethods = "logIn", description = "check opening the list of Bussiness Trips")
    @Parameters({"baseUrl"})
    public void openListOfBT(String baseUrl) {
        String sectionName = "Business Trips";
        driver.get(baseUrl + BUSINESS_TRIP_LOCATION);
        String result = driver.findElement(By.xpath("//td[@class='header1']/h1"))
                .getText();
        Assert.assertEquals(result, sectionName, "The section did not found");
    }

    @Test(dependsOnMethods = "openListOfBT", description = "create new BT")
    @Parameters({"projectName", "country", "destinationCity", "destinationAddress"})
    public void createNewBt(String projectName, String country, String destinationCity, String destinationAddress) throws InterruptedException {
        Date date = new Date();
        String description = "Travel to " + destinationCity + " " + sdf.format(date);
        Integer estimatedBudget = new Random().nextInt(100000);
        String plannedStartDate = "06/11/17";
        String plannedEndDate = "10/11/17";
        By SAVE_LOCATOR = By.xpath("//*[@id='saveButton']/button");
        final By PLANNING_DURATION_LOCATOR = By.xpath("//span[@id='plannedDuration']");
        By ID_LINK_LOCATOR = By.xpath("//span//a[@onclick='animateDetailsLoading()']");
        driver.findElement(By.xpath("//input[@title='Create New Business Trip Request']"))
                .click();
        driver.findElement(By.xpath("//img[contains(@onclick,'chooseprojectcostobject')]"))
                .click();
        driver.switchTo().frame("frLookupDialog");
        driver.findElement(By.xpath("//input[@name='keywordSearch']"))
                .sendKeys(projectName);
        driver.findElement(By.xpath("//input[@value='Go']"))
                .click();
        driver.findElement(By.xpath("//input[@type='checkbox' and @projectcostobjectname='" + projectName + "']"))
                .click();
        driver.findElement(By.xpath("//input[@value='OK']"))
                .click();
        waitForElementEnabled(By.xpath("//input[@id='plannedEndDate_ui']"));
        Select countrySelect = new Select(driver.findElement(By.xpath(".//select[@name='destinationCountryId']")));
        countrySelect
                .selectByVisibleText(country);
        driver.findElement(By.xpath("//input[@name='destinationCity']"))
                .sendKeys(destinationCity);
        driver.findElement(By.xpath("//textarea[@name='destinationAddress']"))
                .sendKeys(destinationAddress);
        driver.findElement(By.xpath("//textarea[@id='description']"))
                .sendKeys(description);
        if ( !driver.findElement(By.xpath("//*[@id='ticketsRequired']")).isSelected() ){
            driver.findElement(By.xpath("//*[@id='ticketsRequired']")).click();
        }
        if ( !driver.findElement(By.xpath("//*[@id='carRequired']")).isSelected() ){
            driver.findElement(By.xpath("//*[@id='carRequired']")).click();
        }
        driver.findElement(By.xpath("//input[@class='textfield textfieldDigit textfieldAmount' and @name='estimatedBudget']"))
                .sendKeys(estimatedBudget.toString());
        driver.findElement(By.xpath("//input[@id='plannedStartDate_ui']"))
                .sendKeys(plannedStartDate);
        driver.findElement(By.xpath("//input[@id='plannedEndDate_ui']"))
                .sendKeys(plannedEndDate);
        driver.findElement(By.xpath("//input[@name='itemName']"))
                .sendKeys("BT created by Selenium " + sdf.format(date));
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d){
                String result = d.findElement(PLANNING_DURATION_LOCATOR).getText();
                return (!result.toLowerCase().equals("0"));
            }
        });
        String executeString = driver.findElement(SAVE_LOCATOR).getAttribute("onclick");
        ((JavascriptExecutor)driver).executeScript(executeString);
        Assert.assertTrue(checkIDLink(ID_LINK_LOCATOR), "Business Trip is not created");


    }
    @Test(dependsOnMethods = "createNewBt", description = "Log out")
    public void logOut() throws InterruptedException {
        By LOGOUT_LOCATOR = By.xpath("//a[@href='logout.do']");
        String textWelcome = "Welcome to EPAM Cost Tracking Center";
        waitForElementVisible(LOGOUT_LOCATOR);
        waitForElementEnabled(LOGOUT_LOCATOR);
        driver.findElement(LOGOUT_LOCATOR).click();
        Alert alert = driver.switchTo().alert();
        alert.accept();
        String result = driver.findElement(By.xpath("//td[@class='header1']/h1"))
                .getText();
        Assert.assertEquals(result, textWelcome, "Logout is not performed");
    }

    @BeforeClass (description = "Start browser")
    public void initBrowser() {
        driver = new FirefoxDriver();
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().window().setSize(new Dimension(1600, 900));

    }

    @AfterClass(description = "close browser")
    public void afterClass() {
        driver.quit();
    }

    protected void waitForElementPresent(By locator) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    protected void waitForElementVisible(By locator) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected void waitForElementEnabled(By locator) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean checkIDLink (By locator){
        String result = driver.findElement(locator).getText();
        if (result.length() == 19){
            return true;
        } else return false;
    }
}
