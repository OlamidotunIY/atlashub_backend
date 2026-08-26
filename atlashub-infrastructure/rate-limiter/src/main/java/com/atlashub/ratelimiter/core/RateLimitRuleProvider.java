package com.atlashub.ratelimiter.core;

import com.atlashub.ratelimiter.core.RateLimitRule;

public interface RateLimitRuleProvider {
    /**
     * Retrieves the rate limit rule for a specific rule ID.
     * @param ruleId The ID of the rule
     * @return The rule, or a default rule if not found
     */
    RateLimitRule getRule(String ruleId);
}
