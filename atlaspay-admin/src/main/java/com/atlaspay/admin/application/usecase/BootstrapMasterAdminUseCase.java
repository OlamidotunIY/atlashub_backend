package com.atlaspay.admin.application.usecase;

import com.atlaspay.admin.application.command.BootstrapMasterAdminCommand;
import com.atlaspay.admin.application.dto.AdminCreationResult;
import com.atlaspay.admin.application.port.out.CloudflareEmailPort;
import com.atlaspay.admin.domain.exception.AdminErrorCode;
import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.domain.model.AdminRole;
import com.atlaspay.admin.domain.model.EmployeeCode;
import com.atlaspay.admin.domain.repository.AdminRepository;
import com.atlaspay.admin.application.service.AdminCreationService;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapMasterAdminUseCase extends BaseUseCase<BootstrapMasterAdminCommand, AdminCreationResult> {

    private final AdminRepository adminRepository;
    private final CloudflareEmailPort cloudflareEmailPort;
    private final AdminCreationService adminCreationService;

    public BootstrapMasterAdminUseCase(AdminRepository adminRepository, 
                                       CloudflareEmailPort cloudflareEmailPort, 
                                       AdminCreationService adminCreationService) {
        this.adminRepository = adminRepository;
        this.cloudflareEmailPort = cloudflareEmailPort;
        this.adminCreationService = adminCreationService;
    }

    @Override
    @Transactional
    public AdminCreationResult execute(BootstrapMasterAdminCommand command) {
        if (adminRepository.existsByRole(AdminRole.MASTER)) {
            throw new ConflictException(AdminErrorCode.MASTER_ADMIN_ALREADY_EXISTS, "A Master Admin already exists");
        }

        String username = command.username();
        if (adminRepository.existsByUsername(username)) {
            throw new ConflictException(AdminErrorCode.ADMIN_USERNAME_ALREADY_EXISTS, "Admin username is already taken");
        }

        EmployeeCode code = EmployeeCode.generate();
        EmailAddress email = new EmailAddress(username + "@atlaspay.name.ng");

        cloudflareEmailPort.createEmailRoutingRule(email.value(), "dotun@atlaspay.name.ng");

        Admin admin = Admin.create(
            adminRepository.nextIdentity(),
            username,
            email,
            AdminRole.MASTER,
            null,
            null
        );

        adminCreationService.saveAndPublishCreationEvent(admin, code);

        return new AdminCreationResult(admin.getUsername(), admin.getEmail().value(), code.formatted());
    }
}
