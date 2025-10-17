package com.inventory.registration.service;

import com.inventory.registration.dto.ProductRegistrationRequest;
import com.inventory.registration.entity.ProductRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BunjangRegistrationService {
    
    @Value("${platforms.bunjang.base-url}")
    private String bunjangBaseUrl;
    
    @Value("${automation.browser.headless}")
    private Boolean headless;
    
    @Value("${automation.browser.timeout}")
    private Integer timeout;
    
    public ProductRegistration registerProduct(ProductRegistrationRequest request) {
        log.info("Registering product on Bunjang using automation: {}", request.getProductName());
        
        WebDriver driver = null;
        try {
            // Setup Chrome driver
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless");
            }
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeout));
            
            // Navigate to Bunjang login page
            driver.get(bunjangBaseUrl + "/login");
            
            // Login process (would need actual credentials)
            // This is a simplified example - actual implementation would need credential management
            loginToBunjang(driver, wait);
            
            // Navigate to product registration page
            driver.get(bunjangBaseUrl + "/products");
            
            // Fill product information
            fillProductForm(driver, wait, request);
            
            // Submit product
            submitProduct(driver, wait);
            
            // Process response
            return processBunjangResponse(request, driver);
            
        } catch (Exception e) {
            log.error("Failed to register product on Bunjang: {}", e.getMessage(), e);
            throw new RuntimeException("Bunjang registration failed: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    
    private void loginToBunjang(WebDriver driver, WebDriverWait wait) {
        // This is a placeholder - actual implementation would need:
        // 1. Get credentials from token service
        // 2. Handle 2FA if required
        // 3. Handle captcha if present
        
        log.info("Logging into Bunjang...");
        // Implementation would go here
    }
    
    private void fillProductForm(WebDriver driver, WebDriverWait wait, ProductRegistrationRequest request) {
        log.info("Filling product form for: {}", request.getProductName());
        
        try {
            // Product name
            WebElement nameField = wait.until(webDriver -> webDriver.findElement(By.name("name")));
            nameField.clear();
            nameField.sendKeys(request.getProductName());
            
            // Product description
            WebElement descriptionField = driver.findElement(By.name("description"));
            descriptionField.clear();
            descriptionField.sendKeys(request.getProductDescription());
            
            // Price
            WebElement priceField = driver.findElement(By.name("price"));
            priceField.clear();
            priceField.sendKeys(request.getPrice().toString());
            
            // Category
            if (request.getCategory() != null) {
                WebElement categoryField = driver.findElement(By.name("category"));
                categoryField.sendKeys(request.getCategory());
            }
            
            // Additional fields
            if (request.getBrand() != null) {
                WebElement brandField = driver.findElement(By.name("brand"));
                brandField.sendKeys(request.getBrand());
            }
            
            if (request.getModel() != null) {
                WebElement modelField = driver.findElement(By.name("model"));
                modelField.sendKeys(request.getModel());
            }
            
            if (request.getColor() != null) {
                WebElement colorField = driver.findElement(By.name("color"));
                colorField.sendKeys(request.getColor());
            }
            
            if (request.getSize() != null) {
                WebElement sizeField = driver.findElement(By.name("size"));
                sizeField.sendKeys(request.getSize());
            }
            
            // Images upload (simplified)
            if (request.getImages() != null && request.getImages().length > 0) {
                WebElement imageField = driver.findElement(By.name("images"));
                // Image upload implementation would go here
            }
            
        } catch (Exception e) {
            log.error("Failed to fill product form: {}", e.getMessage());
            throw new RuntimeException("Failed to fill product form: " + e.getMessage());
        }
    }
    
    private void submitProduct(WebDriver driver, WebDriverWait wait) {
        log.info("Submitting product...");
        
        try {
            WebElement submitButton = wait.until(webDriver -> webDriver.findElement(By.xpath("//button[contains(text(), '등록')]")));
            submitButton.click();
            
            // Wait for submission to complete
            wait.until(webDriver -> webDriver.getCurrentUrl().contains("success") || webDriver.getCurrentUrl().contains("complete"));
            
        } catch (Exception e) {
            log.error("Failed to submit product: {}", e.getMessage());
            throw new RuntimeException("Failed to submit product: " + e.getMessage());
        }
    }
    
    private ProductRegistration processBunjangResponse(ProductRegistrationRequest request, WebDriver driver) {
        ProductRegistration registration = new ProductRegistration();
        registration.setPlatform("bunjang");
        registration.setProductId(request.getProductId());
        registration.setProductName(request.getProductName());
        registration.setProductDescription(request.getProductDescription());
        registration.setStatus("SUCCESS");
        registration.setStartedAt(java.time.LocalDateTime.now());
        registration.setCompletedAt(java.time.LocalDateTime.now());
        
        try {
            // Extract product ID and URL from the page
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("product/")) {
                String productId = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
                registration.setPlatformProductId(productId);
                registration.setPlatformUrl(currentUrl);
            }
        } catch (Exception e) {
            log.warn("Failed to extract product information from page: {}", e.getMessage());
        }
        
        return registration;
    }
}
