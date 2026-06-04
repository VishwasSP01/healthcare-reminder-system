package com.tekravio.healthcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HealthcareReminderApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthcareReminderApplication.class, args);
    }
}

