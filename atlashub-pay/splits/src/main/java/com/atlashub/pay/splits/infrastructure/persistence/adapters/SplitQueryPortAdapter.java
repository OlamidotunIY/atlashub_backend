package com.atlashub.pay.splits.infrastructure.persistence.adapters;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.shared.application.port.SplitQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SplitQueryPortAdapter implements SplitQueryPort {

    private final SplitRuleRepository splitRuleRepository;

    public SplitQueryPortAdapter(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @Override
    public Optional<SplitRuleDto> findActiveSplitRule(Long splitRuleId) {
        return splitRuleRepository.findById(splitRuleId)
                .filter(SplitRule::isActive)
                .map(this::toDto);
    }

    private SplitRuleDto toDto(SplitRule rule) {
        return new SplitRuleDto(
                rule.getId(),
                rule.getOrganizationId(),
                rule.getName(),
                rule.getType() != null ? rule.getType().name() : null,
                rule.getPlatformFeePercentage(),
                rule.getSubaccounts().stream()
                        .map(s -> new SplitSubaccountDto(s.getId(), s.getRecipientId(), s.getShare()))
                        .collect(Collectors.toList()),
                rule.isActive()
        );
    }
}
