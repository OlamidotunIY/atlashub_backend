package com.atlashub.analytics.presentation.dto;

public record HeadcountDto(
        int total,
        int active,
        int onLeave,
        int suspended,
        int terminated,
        int newHiresThisMonth
) {}
