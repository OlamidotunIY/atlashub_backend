package com.atlashub.notifications.adapter.out.external.smtp;

import com.atlashub.notifications.application.port.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Year;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public SmtpEmailSenderAdapter(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context) {
        log.info("Preparing to send HTML email '{}' to {}", templateName, toEmail);
        try {
            // Provide common variables to all templates
            context.setVariable("currentYear", Year.now().getValue());
            
            String htmlContent = templateEngine.process("emails/" + templateName, context);
            
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            javaMailSender.send(message);
            log.info("Successfully sent HTML email '{}' to {}", templateName, toEmail);
        } catch (MessagingException e) {
            log.error("MessagingException occurred while constructing/sending email '{}' to {}", templateName, toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending email '{}' to {}", templateName, toEmail, e);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        sendHtmlEmail(toEmail, "AtlasHub - Verify your email address", "verification", context);
    }

    @Override
    public void sendAdminWelcomeEmail(String toEmail, String temporaryPassword) {
        Context context = new Context();
        context.setVariable("temporaryPassword", temporaryPassword);
        sendHtmlEmail(toEmail, "Welcome to AtlasHub - Admin Account Created", "admin-welcome", context);
    }

    @Override
    public void sendUserSetupPasswordEmail(String toEmail, String firstName, String token) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("setupLink", "https://atlashub.name.ng/auth/setup-password?token=" + token);
        sendHtmlEmail(toEmail, "AtlasHub - Setup your password", "user-setup-password", context);
    }

    @Override
    public void sendPasswordChangedEmail(String toEmail, String firstName) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        sendHtmlEmail(toEmail, "AtlasHub - Your password was changed", "password-changed", context);
    }

    @Override
    public void sendOrganizationJoinedEmail(String toEmail, String firstName, String organizationName) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("organizationName", organizationName);
        sendHtmlEmail(toEmail, "AtlasHub - Welcome to " + organizationName, "organization-joined", context);
    }

    @Override
    public void sendLoginNotificationEmail(String toEmail, String firstName, String ipAddress, String device, String loginTime) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("ipAddress", ipAddress);
        context.setVariable("device", device);
        context.setVariable("loginTime", loginTime);
        sendHtmlEmail(toEmail, "AtlasHub - New Login Alert", "login-notification", context);
    }
}
