package com.tekravio.healthcare.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(long fcmRetryDelayMs) {
}

