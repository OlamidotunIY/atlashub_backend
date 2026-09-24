package com.atlashub.accounts.application.command.SwitchActiveOrganization;

import com.atlashub.accounts.domain.exception.UserNotFoundException;
import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SwitchActiveOrganizationHandler extends Command<SwitchActiveOrganizationCommand, SwitchActiveOrganizationResult> {

    private static final Logger log = LoggerFactory.getLogger(SwitchActiveOrganizationHandler.class);
    private final UserRepository userRepository;
    private final MembershipQueryPort membershipQueryPort;

    public SwitchActiveOrganizationHandler(UserRepository userRepository,
                                           MembershipQueryPort membershipQueryPort) {
        this.userRepository = userRepository;
        this.membershipQueryPort = membershipQueryPort;
    }

    @Override
    @Transactional
    public SwitchActiveOrganizationResult execute(SwitchActiveOrganizationCommand command) {
        log.info("Switching active organization for user id: {}", command.userId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!membershipQueryPort.isMemberOf(command.userId(), command.orgId())) {
            throw new AuthorizationException("User is not a member of the target organization");
        }

        user.switchActiveOrganization(command.orgId());
        userRepository.save(user);

        log.info("Active organization switched to: {}", command.orgId());
        return new SwitchActiveOrganizationResult(user);
    }
}
