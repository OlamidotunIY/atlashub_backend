package com.atlashub.iam.application.commands.UpdateCustomRole;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UpdateCustomRoleHandler extends Command<UpdateCustomRoleCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateCustomRoleHandler.class);
    
    private final CustomRoleRepository customRoleRepository;

    public UpdateCustomRoleHandler(CustomRoleRepository customRoleRepository) {
        this.customRoleRepository = customRoleRepository;
    }

    @Override
    public Void execute(UpdateCustomRoleCommand command) {
        log.info("Executing UpdateCustomRoleCommand");
        
        CustomRole role = customRoleRepository.findById(command.roleId())
            .orElseThrow(() -> new IllegalArgumentException("CustomRole not found: " + command.roleId()));
            
        if (command.name() != null) {
            role.rename(command.name());
        }
        
        if (command.description() != null) {
            role.updateDescription(command.description());
        }
        
        if (command.permissionIds() != null) {
            role.updatePermissions(command.permissionIds());
        }
        
        customRoleRepository.save(role);

        return null;
    }
}
