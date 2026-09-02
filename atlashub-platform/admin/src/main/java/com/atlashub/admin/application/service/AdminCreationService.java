package com.atlashub.admin.application.service;

import com.atlashub.admin.domain.event.AdminCreatedEvent;
import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.model.EmployeeCode;
import com.atlashub.admin.domain.repository.AdminRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import org.springframework.stereotype.Service;

@Service
public class AdminCreationService {

    private final AdminRepository adminRepository;
    private final DomainEventPublisher eventPublisher;

    public AdminCreationService(AdminRepository adminRepository, DomainEventPublisher eventPublisher) {
        this.adminRepository = adminRepository;
        this.eventPublisher = eventPublisher;
    }

    public void saveAndPublishCreationEvent(Admin admin, EmployeeCode code) {
        adminRepository.save(admin);
        
        AdminCreatedEvent event = new AdminCreatedEvent(
            admin.getId(),
            admin.getUsername(),
            admin.getEmail().value(),
            admin.getRole().name(),
            code.rawCode(), 
            admin.getCreatedBy()
        );
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
    }
}
