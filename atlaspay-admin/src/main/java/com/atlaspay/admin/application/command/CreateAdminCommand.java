package com.atlaspay.admin.application.command;

import com.atlaspay.admin.domain.model.AdminPermission;
import java.util.Set;

public record CreateAdminCommand(
    Long requestingAdminId, 
    String username, 
    String destinationEmail, 
    Set<AdminPermission> initialPermissions
) {}
