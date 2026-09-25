package com.atlashub.iam.application.commands.DeleteCustomRole;

import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeleteCustomRoleHandler extends Command<DeleteCustomRoleCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteCustomRoleHandler.class);
    private final CustomRoleRepository customRoleRepository;

    public DeleteCustomRoleHandler(CustomRoleRepository customRoleRepository) {
        this.customRoleRepository = customRoleRepository;
    }

    @Override
    public Void execute(DeleteCustomRoleCommand command) {
        log.info("Executing DeleteCustomRoleCommand");
        
        customRoleRepository.deleteById(command.roleId());

        return null;
    }
}
