package com.atlashub.notifications.domain.ports;

public interface EmailPort {
    void sendHtml(String recipientEmail, String subject, String htmlBody);
}
