package com.atlashub.analytics.application.query.GetCashierLeaderboard;

import java.time.LocalDate;

public record GetCashierLeaderboardQuery(
        Long organizationId,
        String environment,
        LocalDate date
) {}
