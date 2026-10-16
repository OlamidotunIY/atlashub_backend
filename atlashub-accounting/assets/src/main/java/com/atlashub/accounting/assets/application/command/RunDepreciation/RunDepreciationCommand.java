package com.atlashub.accounting.assets.application.command.RunDepreciation;

import java.time.LocalDate;

public record RunDepreciationCommand(
        Long organizationId,
        LocalDate asOfDate
) {}
