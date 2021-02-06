package dev.kenyiu.whatsapp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author kenyiu
 */
@SpringBootApplication
public class WebBatchMessageApp {

    public static void main(String[] args) {
        SpringApplication.run(WebBatchMessageApp.class, args);
    }

    @Bean
    CommandLineRunner runner(
        MessageSender sender,
        @Value("${contacts.path}") String contactsPath,
        @Value("${image.path}") String imagePath
    ) {
        return args -> {
            List<String> contacts = Files.readAllLines(Paths.get(contactsPath));
            Optional<String> imagePathOptional
                = imagePath.isBlank()
                ? Optional.empty()
                : Optional.of(imagePath);
            sender.run(contacts, imagePathOptional);
        };
    }
}
