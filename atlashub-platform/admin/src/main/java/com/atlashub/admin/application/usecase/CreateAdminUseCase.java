package com.atlashub.admin.application.usecase;

import com.atlashub.admin.application.command.CreateAdminCommand;
import com.atlashub.admin.application.result.AdminCreationResult;
import com.atlashub.admin.application.port.CloudflareEmailPort;
import com.atlashub.admin.domain.exception.AdminErrorCode;
import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.valueobject.AdminPermission;
import com.atlashub.admin.domain.valueobject.AdminRole;
import com.atlashub.admin.domain.model.EmployeeCode;
import com.atlashub.admin.domain.repository.AdminRepository;
import com.atlashub.admin.application.service.AdminCreationService;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.exception.BusinessRuleException;
import com.atlashub.shared.exception.ConflictException;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAdminUseCase extends BaseUseCase<CreateAdminCommand, AdminCreationResult> {

    private final AdminRepository adminRepository;
    private final CloudflareEmailPort cloudflareEmailPort;
    private final AdminCreationService adminCreationService;

    public CreateAdminUseCase(AdminRepository adminRepository, 
                              CloudflareEmailPort cloudflareEmailPort, 
                              AdminCreationService adminCreationService) {
        this.adminRepository = adminRepository;
        this.cloudflareEmailPort = cloudflareEmailPort;
        this.adminCreationService = adminCreationService;
    }

    @Override
    @Transactional
    public AdminCreationResult execute(CreateAdminCommand command) {
        Admin requestingAdmin = adminRepository.findById(command.requestingAdminId())
                .orElseThrow(() -> new NotFoundException(AdminErrorCode.ADMIN_NOT_FOUND, "Admin not found"));

        if (!requestingAdmin.hasPermission(AdminPermission.MANAGE_ADMINS)) {
            throw new BusinessRuleException(AdminErrorCode.INSUFFICIENT_PERMISSIONS, "You do not have permission to manage admins");
        }

        String username = command.username();
        if (adminRepository.existsByUsername(username)) {
            throw new ConflictException(AdminErrorCode.ADMIN_USERNAME_ALREADY_EXISTS, "Admin username is already taken");
        }

        EmployeeCode code = EmployeeCode.generate();
        EmailAddress email = new EmailAddress(username + "@atlashub.name.ng");

        cloudflareEmailPort.createEmailRoutingRule(email.value(), command.destinationEmail());

        Admin admin = Admin.create(
            adminRepository.nextIdentity(),
            username,
            email,
            AdminRole.STANDARD,
            requestingAdmin.getId(),
            command.initialPermissions()
        );

        adminCreationService.saveAndPublishCreationEvent(admin, code);

        return new AdminCreationResult(admin.getUsername(), admin.getEmail().value(), code.formatted());
    }
}
