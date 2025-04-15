package com.fxapp.currencyconversion;

import org.springframework.boot.CommandLineRunner;

public class SwaggerConfig implements CommandLineRunner {
    @Override
    public void run(String... args) {
        String url = "http://localhost:8080/swagger-ui.html";
        System.out.println("\nSwagger up at: " + url + "\n");
    }
}
