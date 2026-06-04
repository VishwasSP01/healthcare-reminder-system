package com.tekravio.healthcare.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.twilio")
public record TwilioProperties(String accountSid, String authToken, String whatsappFrom) {
}

