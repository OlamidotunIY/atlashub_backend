package com.atlashub.accounting.gl.application.command.ConfigureLedgerMapping;

import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;

public record ConfigureLedgerMappingCommand(
        Long organizationId,
        SourceSystem sourceSystem,
        Long debitAccountId,
        Long creditAccountId,
        String description
) {}
