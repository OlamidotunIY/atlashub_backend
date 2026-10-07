package com.atlashub.notifications.infrastructure.services;

import com.atlashub.notifications.infrastructure.persistence.adapters.SmtpEmailSenderAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@EnabledIfSystemProperty(named = "atlashub.smtp.smoke.recipient", matches = ".+")
class SmtpEmailSenderAdapterSmokeTest {
    @Test
    void sendsOptInSmokeEmail() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(required("ATLASHUB_SMTP_HOST"));
        sender.setPort(Integer.parseInt(required("ATLASHUB_SMTP_PORT")));
        sender.setUsername(required("ATLASHUB_SMTP_USERNAME"));
        sender.setPassword(required("ATLASHUB_SMTP_PASSWORD"));
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", required("ATLASHUB_SMTP_AUTH"));
        properties.put("mail.smtp.starttls.enable", required("ATLASHUB_SMTP_STARTTLS"));
        properties.put("mail.smtp.ssl.enable", required("ATLASHUB_SMTP_SSL"));
        new SmtpEmailSenderAdapter(sender, required("ATLASHUB_SMTP_FROM"))
                .sendHtml(System.getProperty("atlashub.smtp.smoke.recipient"), "AtlasHub SMTP delivery test",
                        "<html><body><h2>AtlasHub email delivery is working</h2><p>This is a one-time SMTP smoke test.</p></body></html>");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " must be configured");
        return value;
    }
}
