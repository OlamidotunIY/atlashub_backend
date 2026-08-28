package com.atlashub.notifications.application.port;

public interface EmailSenderPort {
    void sendVerificationEmail(String toEmail, String verificationCode);
    void sendAdminWelcomeEmail(String toEmail, String temporaryPassword);
    void sendUserSetupPasswordEmail(String toEmail, String firstName, String token);
    void sendPasswordChangedEmail(String toEmail, String firstName);
    void sendOrganizationJoinedEmail(String toEmail, String firstName, String organizationName);
    void sendLoginNotificationEmail(String toEmail, String firstName, String ipAddress, String device, String loginTime);
}

