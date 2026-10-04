package com.atlashub.iam.application.commands.CreateCustomRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.exception.InvalidPermissionDataException;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CreateCustomRoleHandler extends Command<CreateCustomRoleCommand, CustomRole> {

    private static final Logger log = LoggerFactory.getLogger(CreateCustomRoleHandler.class);
    
    private final CustomRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public CreateCustomRoleHandler(CustomRoleRepository roleRepository,
                                   PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public CustomRole execute(CreateCustomRoleCommand command) {
        log.info("Executing CreateCustomRoleCommand");
        
        validatePermissions(command.permissionIds());
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

    private void validatePermissions(java.util.Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new InvalidPermissionDataException("At least one active permission is required");
        }
        long validPermissionCount = permissionRepository.findAllById(permissionIds).stream()
                .filter(Permission::isActive)
                .map(Permission::getId)
                .distinct()
                .count();
        if (validPermissionCount != permissionIds.size()) {
            throw new InvalidPermissionDataException("Every role permission must exist and be active");
        }
    }
}
