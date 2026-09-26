package com.atlashub.notifications.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.notifications.domain.valueobject.NotificationCategory;
import com.atlashub.notifications.domain.valueobject.NotificationChannel;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
public class UserNotificationPreference extends AggregateRoot<Long> {

    private final Long id;
    private final Long userId;
    private final NotificationCategory category;
    private final Set<NotificationChannel> enabledChannels;
    private ZonedDateTime updatedAt;

    private UserNotificationPreference(Long id, Long userId, NotificationCategory category, Set<NotificationChannel> enabledChannels, ZonedDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.enabledChannels = new HashSet<>(enabledChannels);
        this.updatedAt = updatedAt;
    }

    public static UserNotificationPreference create(Long id, Long userId, NotificationCategory category, Set<NotificationChannel> enabledChannels) {
        return new UserNotificationPreference(id, userId, category, enabledChannels != null ? enabledChannels : new HashSet<>(), ZonedDateTime.now());
    }

    public void enableChannel(NotificationChannel channel) {
        if (channel != null && this.enabledChannels.add(channel)) {
            touch();
        }
    }

    public void disableChannel(NotificationChannel channel) {
        if (channel != null && this.enabledChannels.remove(channel)) {
            touch();
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
