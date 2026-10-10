package com.atlashub.pay.ledger.presentation.dto;import java.time.ZonedDateTime;public record AccountTransferResponse(Long transactionId,String reference,ZonedDateTime postedAt){}
