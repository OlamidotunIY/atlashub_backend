package com.atlashub.analytics.application.query.GetBusinessOverview;

import java.time.LocalDate;

public record GetBusinessOverviewQuery(Long organizationId, String environment, LocalDate asOfDate) {
}
