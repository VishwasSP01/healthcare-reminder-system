package com.tekravio.healthcare.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap")
public record AdminBootstrapProperties(String adminEmail, String adminPassword) {
}

