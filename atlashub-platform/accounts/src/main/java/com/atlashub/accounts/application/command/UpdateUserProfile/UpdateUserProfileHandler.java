package com.atlashub.accounts.application.command.UpdateUserProfile;

import com.atlashub.accounts.domain.exception.UserNotFoundException;
import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class UpdateUserProfileHandler extends BaseUseCase<UpdateUserProfileCommand, UpdateUserProfileResult> {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserProfileHandler.class);
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateUserProfileHandler(UserRepository userRepository, DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

        publishEvents(user, eventPublisher);

        return new UpdateUserProfileResult(user);
    }
}
