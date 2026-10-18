package com.atlashub.shared.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SplitQueryPort {

    Optional<SplitRuleDto> findActiveSplitRule(Long splitRuleId);

    record SplitRuleDto(
            Long id,
            Long organizationId,
            String name,
            String type,
            BigDecimal platformFeePercentage,
            List<SplitSubaccountDto> subaccounts,
            boolean active
    ) {
    }

    record SplitSubaccountDto(
            Long id,
            String subaccountCode,
            BigDecimal share
    ) {
    }
}
