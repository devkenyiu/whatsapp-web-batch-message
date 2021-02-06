package dev.kenyiu.whatsapp;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author kenyiu
 */
@Configuration
public class Config {

    @Bean
    WebDriver webDriver(@Value("${geckodriver.path}") String pathToGeckoDriver) {
        System.setProperty("webdriver.gecko.driver", pathToGeckoDriver);
        WebDriver driver = new FirefoxDriver();
        return driver;
    }

}
