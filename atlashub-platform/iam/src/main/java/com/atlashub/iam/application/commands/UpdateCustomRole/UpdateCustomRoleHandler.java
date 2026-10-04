package com.atlashub.iam.application.commands.UpdateCustomRole;

import com.atlashub.shared.application.usecase.Command;
import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.exception.InvalidPermissionDataException;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UpdateCustomRoleHandler extends Command<UpdateCustomRoleCommand, CustomRole> {

    private static final Logger log = LoggerFactory.getLogger(UpdateCustomRoleHandler.class);
    
    private final CustomRoleRepository customRoleRepository;
    private final PermissionRepository permissionRepository;

    public UpdateCustomRoleHandler(CustomRoleRepository customRoleRepository,
                                   PermissionRepository permissionRepository) {
        this.customRoleRepository = customRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public CustomRole execute(UpdateCustomRoleCommand command) {
        log.info("Executing UpdateCustomRoleCommand");
        
        CustomRole role = customRoleRepository.findById(command.roleId())
            .filter(found -> found.getOrganizationId().equals(command.organizationId()))
            .orElseThrow(() -> new IllegalArgumentException("CustomRole not found: " + command.roleId()));
            
        if (command.name() != null) {
            role.rename(command.name());
        }
        
        if (command.description() != null) {
            role.updateDescription(command.description());
        }
        
        if (command.permissionIds() != null) {
            validatePermissions(command.permissionIds());
            role.updatePermissions(command.permissionIds());
        }
        
        customRoleRepository.save(role);

        return role;
    }

    private void validatePermissions(java.util.Set<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
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
