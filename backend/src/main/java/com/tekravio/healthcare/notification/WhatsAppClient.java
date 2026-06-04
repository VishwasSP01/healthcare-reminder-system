package com.tekravio.healthcare.notification;

public interface WhatsAppClient {

    String send(WhatsAppMessageRequest request);
}

