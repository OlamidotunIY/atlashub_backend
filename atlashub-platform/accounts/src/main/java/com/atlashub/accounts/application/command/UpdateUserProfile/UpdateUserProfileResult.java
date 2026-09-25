package com.atlashub.accounts.application.command.UpdateUserProfile;

import com.atlashub.accounts.domain.model.User;

public record UpdateUserProfileResult(
        User user
) {
}
