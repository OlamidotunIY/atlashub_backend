package com.atlashub.pay.splits.infrastructure.persistence.adapters;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleInactiveException;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.ports.SplitQueryPort;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import org.springframework.stereotype.Component;

@Component
public class SplitQueryPortAdapter implements SplitQueryPort {

    private final SplitRuleRepository splitRuleRepository;

    public SplitQueryPortAdapter(SplitRuleRepository splitRuleRepository) {
        this.splitRuleRepository = splitRuleRepository;
    }

    @Override
    public SplitRule findActiveSplitRule(Long splitRuleId) {
        SplitRule rule = splitRuleRepository.findById(splitRuleId)
                .orElseThrow(SplitRuleNotFoundException::new);
        
        if (!rule.isActive()) {
            throw new SplitRuleInactiveException();
        }
        
        return rule;
    }
}
