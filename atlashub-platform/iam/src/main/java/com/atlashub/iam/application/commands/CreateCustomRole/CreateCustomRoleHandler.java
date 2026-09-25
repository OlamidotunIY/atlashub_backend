package com.atlashub.iam.application.commands.CreateCustomRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CreateCustomRoleHandler extends Command<CreateCustomRoleCommand, CustomRole> {

    private static final Logger log = LoggerFactory.getLogger(CreateCustomRoleHandler.class);
    
    private final CustomRoleRepository roleRepository;

    public CreateCustomRoleHandler(CustomRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public CustomRole execute(CreateCustomRoleCommand command) {
        log.info("Executing CreateCustomRoleCommand");
        
        Long id = roleRepository.nextIdentity();
        
        CustomRole newRole = CustomRole.create(
                id, 
                command.orgId(), 
                command.name(), 
                command.description(), 
                command.permissionIds(), 
                false,
                command.createdBy()
        );
        
        roleRepository.save(newRole);

        return newRole;
    }
}
