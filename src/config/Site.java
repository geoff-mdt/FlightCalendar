package config;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.interactions.Actions;

import org.openqa.selenium.firefox.FirefoxOptions;

public class Site {

    WebDriver driver;
    Boolean isConnected;
    Actions builder;

    public Site() {
        // Set the driver (for Firefox only)
        System.setProperty("webdriver.gecko.driver", "lib/geckodriver.exe");

        // Set a Firefox profile to disable TLS 1.1 and 1.2 depreciation
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile ffProfile = profile.getProfile("default");
        ffProfile.setPreference("security.tls.version.enable-deprecated", true);
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(ffProfile);

        driver = new FirefoxDriver(options);
        
        /*  // Minimize the browser window
        Point p = driver.manage().window().getPosition();
        Dimension d = driver.manage().window().getSize();
        driver.manage().window().setPosition(new Point((d.getHeight() - p.getX()), (d.getWidth() - p.getY())));

        // Create an Actions class builder (used to hover elements for instance)
        builder = new Actions(driver); */
        isConnected = false;
    }

    /**
     * Connect the driver to oflyers website then connect the users to the website
     */
    public void connect() {
        driver.get("https://oflyers.isae.fr");
        if (!isConnected) {
            // Find the text input element by its name
            WebElement login = driver.findElement(By.id("login"));
            WebElement pass = driver.findElement(By.id("password"));

            // Enter logins in the form
            login.sendKeys(""); //enter login   ex: "gmordelet"
            pass.sendKeys(""); //enter password ex: "password"

            // Now submit the form
            login.submit();

            try {
                // wait until the next element is found for 10s max
                driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
                driver.findElement(By.xpath("//*[@id=\"menu3\"]")); // Search for CAHIER INSTRU menu (ensure connexion
                                                                    // is effective)
                isConnected = true;
            } catch (NoSuchElementException e) {
                isConnected = false;
                System.out.println("Login and/or password do not fit");
                this.quit();
            }
        }
    }

    /**
     * Close the website and the driver
     * @return
     */
    public void quit(){
        driver.quit();
    }

    /**
     * Waits for a class to be visible by the driver. Helps waiting a page to be loaded
     * @return Boolean
     */
    public Boolean waitClass(String elementClass){
        Boolean isVisible = false;
        while (!isVisible) {
            try {
                driver.findElement(By.className(elementClass));
                isVisible = true;
            } catch (Exception e) {
                // Nothing, the exception is raised until the page is charged
            }
        }
        return isVisible;
    }
    /**
     * @return Boolean
     */
    public Boolean getConnexionState() {
        return isConnected;
    }

    /**
     * @return WebDriver
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * @return Actions
     */
    public Actions getBuilder() {
        return builder;
    }
}
