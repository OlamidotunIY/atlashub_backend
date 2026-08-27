package com.atlashub.ledger.adapter.in.web.request;

public record GetLedgerHistoryRequestDto(
    Integer page,
    Integer perPage
) {
    public GetLedgerHistoryRequestDto {
        if (page == null || page < 1) page = 1;
        if (perPage == null || perPage < 1) perPage = 50;
    }
}
