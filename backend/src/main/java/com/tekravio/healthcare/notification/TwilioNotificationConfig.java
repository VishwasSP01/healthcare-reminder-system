package com.tekravio.healthcare.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TwilioProperties.class)
public class TwilioNotificationConfig {

    @Bean
    WhatsAppClient whatsAppClient(TwilioProperties properties) {
        if (isBlank(properties.accountSid()) || isBlank(properties.authToken()) || isBlank(properties.whatsappFrom())) {
            return request -> {
                throw new IllegalStateException("Twilio WhatsApp credentials are not configured");
            };
        }

        Twilio.init(properties.accountSid(), properties.authToken());
        return request -> {
            try {
                Message message = Message.creator(
                                new PhoneNumber(formatWhatsAppNumber(request.to())),
                                new PhoneNumber(properties.whatsappFrom()),
                                request.message())
                        .create();
                return message.getSid();
            } catch (Exception exception) {
                throw new IllegalStateException("WhatsApp send failed", exception);
            }
        };
    }

    private String formatWhatsAppNumber(String phoneNumber) {
        if (phoneNumber.startsWith("whatsapp:")) {
            return phoneNumber;
        }
        return "whatsapp:" + phoneNumber;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

