package com.tekravio.healthcare.notification;

public interface FcmClient {

    String send(FcmMessageRequest request);
}

