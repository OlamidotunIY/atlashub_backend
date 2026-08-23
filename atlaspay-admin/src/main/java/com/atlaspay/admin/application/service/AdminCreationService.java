package com.atlaspay.admin.application.service;

import com.atlaspay.admin.domain.event.AdminCreatedEvent;
import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.domain.model.EmployeeCode;
import com.atlaspay.admin.domain.repository.AdminRepository;
import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
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
