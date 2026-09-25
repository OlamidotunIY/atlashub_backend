package com.atlashub.authentication.application.command.ChangePassword;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordHandler extends Command<ChangePasswordCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public ChangePasswordHandler(AuthAccountRepository accountRepository,
                                 PasswordEncoderPort passwordEncoderPort) {
        this.accountRepository = accountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public Void execute(ChangePasswordCommand command) {
        AuthAccount account = accountRepository.findByUserId(command.userId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!passwordEncoderPort.matches(command.currentPassword(), account.getPassword())) {
            throw new InvalidCredentials();
        }

        String newHash = passwordEncoderPort.encode(command.newPassword());
        account.updatePassword(newHash);
        accountRepository.save(account);

        return null;
    }
}
