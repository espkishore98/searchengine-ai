package com.searchengine.searchengine_ai;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class GoogleSeleniumScraper {
    public List<ScrapeLink> getLinks(String query) {
        // Setup WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Use a Chrome profile to avoid detection
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=/Users/esp/Library/Application Support/Google/Chrome/Default"); // Use your Chrome profile path
        options.addArguments("--disable-blink-features=AutomationControlled"); // Bypass bot detection
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage"); // Required for some environments

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        try {
            // Open Google Search
            String url = "https://www.google.com/search?q=" + query.replace(" ", "+");
            driver.get(url);

//            // Handle CAPTCHA if detected
//            if (isCaptchaPresent(driver)) {
//                System.out.println("CAPTCHA detected! Waiting for manual resolution...");
//                waitForCaptchaResolution(driver);
//            }

            // Wait for results to load
            Thread.sleep(3000);
            humanScroll(driver); // Simulate scrolling

            // Select search result elements
            List<WebElement> results = driver.findElements(By.cssSelector("div.tF2Cxc"));

            List<ScrapeLink> searchResults = results.stream()
                    .map(result -> {
                        ScrapeLink scrapeLink = new ScrapeLink();
                        scrapeLink.setLink(result.findElement(By.tagName("a")).getAttribute("href"));
                        scrapeLink.setTitle(result.findElement(By.tagName("h3")).getText());
                        return scrapeLink;
                    })
                    .collect(Collectors.toList());

            searchResults.forEach(System.out::println);
            return searchResults;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return List.of();
    }

    // Method to detect CAPTCHA
    private boolean isCaptchaPresent(WebDriver driver) {
        try {
            return driver.findElements(By.xpath("//*[contains(text(),'I'm not a robot')]")).size() > 0 ||
                    driver.findElements(By.xpath("//iframe[contains(@src, 'recaptcha')]")).size() > 0;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // Method to wait for CAPTCHA resolution
    private void waitForCaptchaResolution(WebDriver driver) throws InterruptedException {
        while (isCaptchaPresent(driver)) {
            System.out.println("Waiting for user to solve CAPTCHA...");
            Thread.sleep(5000); // Wait and check again
        }
        System.out.println("CAPTCHA resolved!");
    }

    // Method to mimic human scrolling behavior
    private void humanScroll(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < 3; i++) {
            js.executeScript("window.scrollBy(0, 500)");
            try {
                Thread.sleep(1000); // Wait between scrolls
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
