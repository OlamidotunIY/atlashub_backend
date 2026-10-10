package com.atlashub.pay.txquery.application.queries.GetTransactionDetails;
import com.atlashub.shared.application.security.ApiEnvironment;
public record GetTransactionDetailsQuery(Long organizationId,ApiEnvironment environment,Long transactionId){}
