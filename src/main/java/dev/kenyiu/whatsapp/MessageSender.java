package dev.kenyiu.whatsapp;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author kenyiu
 */
@Slf4j
@Component
public class MessageSender {

    private static final String URL_WHATSAPP_WEB = "https://web.whatsapp.com/";

    @Autowired
    private WebDriver webDriver;

    void run(List<String> contacts, Optional<String> imagePath) {
        log.info("running...");
        webDriver.get(URL_WHATSAPP_WEB);

        if (imagePath.isPresent()) {
            log.info("image file path: {}", imagePath.get());
        }
        log.info("Scan the barcode, copy your message, and press <Enter>...");
        Scanner in = new Scanner(System.in);
        in.nextLine();

        List<String> contactsNotFound = new ArrayList<>();
        List<String> contactsSendError = new ArrayList<>();

        for (int i = 0; i < contacts.size(); i++) {
            String contact = contacts.get(i);
            log.info("%d / %d...", ++i, contacts.size());
            if (!findContact(contact)) {
                log.warn("contact not found: {}", contact);
                contactsNotFound.add(contact);
                continue;
            }
            if (!sendMessage(imagePath)) {
                contactsSendError.add(contact);
            }
        }

        log.error("Contacts not found:");
        contactsNotFound.forEach(System.out::println);
        log.error("Contacts with error:");
        contactsSendError.forEach(System.out::println);
    }

    private boolean findContact(String contact) {
        if (contact.strip().isBlank()) {
            return false; // skip empty lines
        }
        // search the contact
        WebElement searchBox
            = webDriver.findElement(
                By.xpath("/html/body/div[1]/div/div/div[3]/div/div[1]/div/label/div/div[2]")
            );
        searchBox.sendKeys(contact);
        searchBox.sendKeys(Keys.ENTER);
        waitForSeconds("searching contact " + contact, 4);

        // check if the contact exists
        try {
            WebElement searchResult
                = webDriver.findElement(
                    By.xpath("/html/body/div[1]/div/div/div[3]/div/div[2]/div[1]/div/div/div[2]/div/div/div/div[2]")
                );
            WebElement contactNameSpan
                = webDriver.findElement(
                    By.xpath("/html/body/div[1]/div/div/div[4]/div/header/div[2]/div[1]/div/span")
                );
            String contactName = contactNameSpan.getText();
            if (!contactName.contains(contact)) {
                searchBox.clear();
                waitForSeconds("clearing search box", 1);
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            searchBox.clear();
            waitForSeconds("clearing search box", 1);
            return false;
        }
    }

    private boolean sendMessage(Optional<String> imagePath) {
        try {
            // paste the copied message and send
            WebElement msgBox = webDriver.findElement(
                By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[2]")
            );
            msgBox.sendKeys(Keys.chord(Keys.CONTROL, "v")); // paste message
            if (!imagePath.isPresent()) {
                WebElement sendBtn = webDriver.findElement(
                    By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[3]/button")
                );
                sendBtn.click();
                waitForSeconds("sending message", 1);
            } else {
                // upload file
                try {
                    WebElement btnAttach = webDriver.findElement(
                        By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[1]/div[2]/div/div")
                    );
                    btnAttach.click();

                    // WARNING: DO NOT click the button to open the file dialog
                    // append the same element with /input
                    WebElement fileChooser = webDriver.findElement(
                        By.xpath("/html/body/div[1]/div/div/div[4]/div/footer/div[1]/div[1]/div[2]/div/span/div/div/ul/li[1]/button/input")
                    );
                    fileChooser.sendKeys(imagePath.get()); // just send the full path of the file

                    waitForSeconds("sending image", 1);

                    WebElement btnSendImage = webDriver.findElement(
                        By.xpath("/html/body/div[1]/div/div/div[2]/div[2]/span/div/span/div/div/div[2]/span/div/div")
                    );
                    btnSendImage.click();

                } catch (Exception ex) {
                    log.error("error sending image", ex);
                    return false;
                }
            }
        } catch (Exception ex) {
            log.error("unknown error", ex);
            return false;
        }
        return true;
    }

    private static void waitForSeconds(String message, int nSeconds) {
        log.info(message);
        Uninterruptibles.sleepUninterruptibly(nSeconds, TimeUnit.SECONDS);
    }
}
