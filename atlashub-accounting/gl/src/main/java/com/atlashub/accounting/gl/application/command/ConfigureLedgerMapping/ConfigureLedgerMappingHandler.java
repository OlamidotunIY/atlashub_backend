package com.atlashub.accounting.gl.application.command.ConfigureLedgerMapping;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.repositories.LedgerAccountMappingRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConfigureLedgerMappingHandler
        extends Command<ConfigureLedgerMappingCommand, ConfigureLedgerMappingResult> {

    private final LedgerAccountMappingRepository mappingRepository;
    private final AccountRepository accountRepository;

    public ConfigureLedgerMappingHandler(LedgerAccountMappingRepository mappingRepository,
                                         AccountRepository accountRepository) {
        this.mappingRepository = mappingRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:mapping:configure')")
    public ConfigureLedgerMappingResult execute(ConfigureLedgerMappingCommand command) {
        boolean debitExists = accountRepository.findById(command.debitAccountId())
                .filter(a -> a.getOrganizationId().equals(command.organizationId()))
                .isPresent();
        if (!debitExists) {
            throw new AccountNotFoundException(command.debitAccountId());
        }

        boolean creditExists = accountRepository.findById(command.creditAccountId())
                .filter(a -> a.getOrganizationId().equals(command.organizationId()))
                .isPresent();
        if (!creditExists) {
            throw new AccountNotFoundException(command.creditAccountId());
        }

        Optional<LedgerAccountMapping> existing = mappingRepository
                .findByOrganizationIdAndSourceSystem(command.organizationId(), command.sourceSystem());

        LedgerAccountMapping mapping;
        if (existing.isPresent()) {
            mapping = existing.get();
            mapping.updateMapping(command.debitAccountId(), command.creditAccountId(), command.description());
        } else {
            Long id = mappingRepository.nextIdentity();
            mapping = LedgerAccountMapping.create(
                    id,
                    command.organizationId(),
                    command.sourceSystem(),
                    command.debitAccountId(),
                    command.creditAccountId(),
                    command.description()
            );
        }

        LedgerAccountMapping saved = mappingRepository.save(mapping);

        return new ConfigureLedgerMappingResult(
                saved.getId(),
                saved.getOrganizationId(),
                saved.getSourceSystem(),
                saved.getDebitAccountId(),
                saved.getCreditAccountId(),
                saved.getDescription()
        );
    }
}
