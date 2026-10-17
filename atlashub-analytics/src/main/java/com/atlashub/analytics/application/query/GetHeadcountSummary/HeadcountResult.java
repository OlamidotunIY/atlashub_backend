package com.atlashub.analytics.application.query.GetHeadcountSummary;

public record HeadcountResult(
        int total,
        int active,
        int onLeave,
        int suspended,
        int terminated,
        int newHiresThisMonth
) {}
