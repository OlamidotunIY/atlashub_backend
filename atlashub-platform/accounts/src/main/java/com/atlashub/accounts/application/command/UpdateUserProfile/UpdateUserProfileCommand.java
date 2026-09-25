package com.atlashub.accounts.application.command.UpdateUserProfile;

import com.atlashub.shared.domain.valueobject.PhoneNumber;

public record UpdateUserProfileCommand(
        Long userId,
        String firstname,
        String lastname,
        PhoneNumber phone
) {
}
