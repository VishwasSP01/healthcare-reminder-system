package com.tekravio.healthcare.notification;

import java.util.Map;

public record FcmMessageRequest(String token, String title, String body, Map<String, String> data) {
}

