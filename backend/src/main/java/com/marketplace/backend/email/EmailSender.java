package com.marketplace.backend.email;

public interface EmailSender {

    void send(String to, String subject, String body);
}
