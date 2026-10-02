package com.atlashub.pay.splits.domain.ports;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleInactiveException;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;

public interface SplitQueryPort {
    /**
     * Returns the active split rule for the given ID.
     * Throws SplitRuleNotFoundException if the rule does not exist.
     * Throws SplitRuleInactiveException if the rule exists but is inactive.
     */
    SplitRule findActiveSplitRule(Long splitRuleId) throws SplitRuleNotFoundException, SplitRuleInactiveException;
}
