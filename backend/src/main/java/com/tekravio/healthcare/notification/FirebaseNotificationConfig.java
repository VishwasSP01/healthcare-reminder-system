package com.tekravio.healthcare.notification;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({FirebaseProperties.class, NotificationProperties.class})
public class FirebaseNotificationConfig {

    @Bean
    FcmClient fcmClient(FirebaseProperties properties) throws IOException {
        if (properties.credentialsPath() == null || properties.credentialsPath().isBlank()) {
            return request -> {
                throw new IllegalStateException("Firebase credentials are not configured");
            };
        }

        FirebaseOptions options;
        try (FileInputStream serviceAccount = new FileInputStream(properties.credentialsPath())) {
            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        }

        FirebaseApp app = FirebaseApp.getApps().stream()
                .filter(existing -> FirebaseApp.DEFAULT_APP_NAME.equals(existing.getName()))
                .findFirst()
                .orElseGet(() -> FirebaseApp.initializeApp(options));

        FirebaseMessaging messaging = FirebaseMessaging.getInstance(app);
        return request -> {
            Message message = Message.builder()
                    .setToken(request.token())
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(request.body())
                            .build())
                    .putAllData(request.data())
                    .build();
            try {
                return messaging.send(message);
            } catch (Exception exception) {
                throw new IllegalStateException("FCM send failed", exception);
            }
        };
    }
}

