package com.atlashub.shared.application.port;

public interface EmailPort {

    void sendHtml(String recipientEmail, String subject, String htmlBody);
}
