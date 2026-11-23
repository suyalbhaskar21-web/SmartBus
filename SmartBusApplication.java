package com.smartbus;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartBusApplication {
    public static void main(String[] args) throws Exception {
        // Create the database folder before Spring connects to SQLite.
        Files.createDirectories(Path.of("data"));
        SpringApplication.run(SmartBusApplication.class, args);
    }
}
