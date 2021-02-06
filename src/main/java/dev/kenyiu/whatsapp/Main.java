package dev.kenyiu.whatsapp;

import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Main {

    private static final String URL_WHATSAPP_WEB = "https://web.whatsapp.com/";
    private static final String CONTACT_FILE = "contacts.txt"; // contact name on each line
    private static final Optional<String> IMAGE_FILE = Optional.of("/path/to/image.jpeg"); // null if no image

    public static void main(String[] args) throws IOException {
        System.setProperty("webdriver.gecko.driver", "/path/to/geckodriver.exe");
        WebDriver driver = new FirefoxDriver();

        driver.get(URL_WHATSAPP_WEB);

        System.out.println("Copy your message, and press <Enter> here:");
        Scanner in = new Scanner(System.in);
        in.nextLine();

        List<String> contactsNotFound = new ArrayList<>();
        List<String> contacts = loadContacts(CONTACT_FILE);
        int i = 0;
        int n = contacts.size();
        for (String contact : contacts) {
            System.out.printf("%d / %d...%n", ++i, n);
            try {
                if (contact.strip().isBlank()) {
                    continue; // skip empty lines
                }
                // search the contact
                WebElement searchBox
                    = driver.findElement(
                        By.xpath("/html/body/div[1]/div/div/div[3]/div/div[1]/div/label/div/div[2]") // business (firefox)
                    );
                searchBox.sendKeys(contact);
                searchBox.sendKeys(Keys.ENTER);
                waitForSeconds("searching contact " + contact, 4);

                // check if the contact exists
                try {
                    WebElement searchResult
                        = driver.findElement(
                            By.xpath("/html/body/div[1]/div/div/div[3]/div/div[2]/div[1]/div/div/div[2]/div/div/div/div[2]") // business (firefox)
                        );

                    WebElement contactNameSpan
                        = driver.findElement(
                            By.xpath("/html/body/div[1]/div/div/div[4]/div/header/div[2]/div[1]/div/span")
                        );
                    String contactName = contactNameSpan.getText();
                    if (!contactName.contains(contact)) {
                        System.out.printf("contact not found: %s%n", contact);
                        contactsNotFound.add(contact);
                        searchBox.clear();
                        waitForSeconds("clearing search box", 1);
                        continue;
                    }
                } catch (Exception ex) {
                    System.out.println("contact not found: " + contact);
                    contactsNotFound.add(contact);
                    searchBox.clear();
                    waitForSeconds("clearing search box", 1);
                    continue;
                }

                // paste the copied message and send
                WebElement msgBox = driver.findElement(
                    By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[2]") // business (firefox)
                );
                msgBox.sendKeys(Keys.chord(Keys.CONTROL, "v")); // paste message
                if (IMAGE_FILE == null) {
                    WebElement sendBtn = driver.findElement(
                        By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[3]/button")
                    );
                    sendBtn.click();
                    waitForSeconds("sending message", 1);
                }

                // upload file
                if (IMAGE_FILE.isPresent()) {
                    try {
                        WebElement btnAttach = driver.findElement(
                            By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[1]/div[2]/div/div") // business (firefox)
                        );
                        btnAttach.click();

                        // WARNING: DO NOT click the button to open the file dialog
                        // append the same element with /input
                        WebElement fileChooser = driver.findElement(
                            By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[1]/div[2]/div/span/div/div/ul/li[1]/button/input") // business (firefox)
                        );
                        fileChooser.sendKeys(IMAGE_FILE.get()); // just send the full path of the file

                        waitForSeconds("sending image", 1);

                        WebElement btnSendImage = driver.findElement(
                            By.xpath("/html/body/div[1]/div/div/div[2]/div[2]/span/div/span/div/div/div[2]/span/div/div") // business (firefox)
                        );
                        btnSendImage.click();

                    } catch (Exception ex) {
                        System.out.println("Error sending image: " + ex.getMessage());
                        ex.printStackTrace();
                        throw ex;
                    }
                }

            } catch (Exception ex) {
                System.out.println("Unknown error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        System.out.println("Contacts Not Found:");
        contactsNotFound.forEach(System.out::println);
    }

    private static List<String> loadContacts(String fileName) throws IOException {
        return Files.readAllLines(Paths.get(fileName));
    }

    private static void waitForSeconds(String message, int nSeconds) {
        System.out.println(message);
        Uninterruptibles.sleepUninterruptibly(nSeconds, TimeUnit.SECONDS);
    }
}
