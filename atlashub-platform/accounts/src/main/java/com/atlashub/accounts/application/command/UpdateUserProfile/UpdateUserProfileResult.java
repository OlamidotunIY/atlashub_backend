package com.atlashub.accounts.application.command.UpdateUserProfile;

import com.atlashub.accounts.domain.entities.User;

public record UpdateUserProfileResult(
        User user
) {
}
