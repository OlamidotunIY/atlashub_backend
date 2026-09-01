package com.atlashub.admin.application.usecase;

import com.atlashub.admin.application.command.BootstrapMasterAdminCommand;
import com.atlashub.admin.application.result.AdminCreationResult;
import com.atlashub.admin.domain.exception.AdminErrorCode;
import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.valueobject.AdminRole;
import com.atlashub.admin.domain.model.EmployeeCode;
import com.atlashub.admin.domain.repository.AdminRepository;
import com.atlashub.admin.application.service.AdminCreationService;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.exception.ConflictException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapMasterAdminUseCase extends BaseUseCase<BootstrapMasterAdminCommand, AdminCreationResult> {

    private final AdminRepository adminRepository;
    private final AdminCreationService adminCreationService;

    public BootstrapMasterAdminUseCase(AdminRepository adminRepository, 
                                       AdminCreationService adminCreationService) {
        this.adminRepository = adminRepository;
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
        EmailAddress email = new EmailAddress(username + "@atlashub.name.ng");

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
