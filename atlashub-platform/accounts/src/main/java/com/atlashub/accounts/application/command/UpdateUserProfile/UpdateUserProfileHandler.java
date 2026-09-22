package com.atlashub.accounts.application.command.UpdateUserProfile;

import com.atlashub.accounts.domain.exception.UserNotFoundException;
import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateUserProfileHandler extends Command<UpdateUserProfileCommand, UpdateUserProfileResult> {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserProfileHandler.class);
    private final UserRepository userRepository;

    public UpdateUserProfileHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UpdateUserProfileResult execute(UpdateUserProfileCommand input) {
        log.info("Updating user profile.....");

        User user = userRepository.findById(input.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.updateProfile(input.firstname(), input.lastname(), input.phone());
        userRepository.save(user);

        log.info("Saved updated user profile");
        return new UpdateUserProfileResult(user);
    }
}
