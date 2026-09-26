package com.atlashub.notifications.domain.entities;

import com.atlashub.notifications.domain.valueobject.NotificationCategory;
import com.atlashub.notifications.domain.valueobject.NotificationChannel;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class NotificationTemplate extends AggregateRoot<Long> {

    private final Long id;
    private String code;
    private NotificationCategory category;
    private NotificationChannel channel;
    private String subject;
    private String bodyTemplate;
    private Integer version;
    private Boolean isActive;
    private ZonedDateTime updatedAt;

    public NotificationTemplate(Long id, String code, NotificationCategory category,
                                NotificationChannel channel, String subject,
                                String bodyTemplate, Integer version,
                                Boolean isActive, ZonedDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.category = category;
        this.channel = channel;
        this.subject = subject;
        this.bodyTemplate = bodyTemplate;
        this.version = version;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
    }

    private NotificationTemplate(String code, NotificationCategory category,
                                 NotificationChannel channel, String subject,
                                 String bodyTemplate) {
        this.id = null;
        this.code = code;
        this.category = category;
        this.channel = channel;
        this.subject = subject;
        this.bodyTemplate = bodyTemplate;
        this.version = 0;
        this.isActive = true;
        this.updatedAt = ZonedDateTime.now();
    }

    public static NotificationTemplate create(String code, NotificationCategory category,
                                              NotificationChannel channel, String subject,
                                              String bodyTemplate) {
        return new NotificationTemplate(code, category, channel, subject, bodyTemplate);
    }

    public void updateTemplate(String subject, String bodyTemplate) {
        this.subject = subject;
        this.bodyTemplate = bodyTemplate;
        touch();
    }

    public void activate() {
        if (!Boolean.TRUE.equals(this.isActive)) {
            this.isActive = true;
            touch();
        }
    }

    public void deactivate() {
        if (!Boolean.FALSE.equals(this.isActive)) {
            this.isActive = false;
            touch();
        }
    }

    private void touch() {
        this.version = (this.version == null ? 0 : this.version) + 1;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
