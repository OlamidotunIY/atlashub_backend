package com.atlashub.shared.application.port;

public interface AnchorBankingPort {
    boolean supports(String capability,String apiEnvironment);
    String requireFboAccountId(String apiEnvironment);
    DepositAccountResult createBusinessDepositAccount(String customerId,String productName,String requestReference,String apiEnvironment);
    SubAccountResult createBusinessSubAccount(String customerId,String parentFboAccountId,boolean createVirtualNuban,String requestReference,String apiEnvironment);
    ReservedAccountResult createReservedAccount(ReservedAccountCustomer customer,String provider,String payoutSubAccountId,String requestReference,String apiEnvironment);
    AccountDetails fetchDepositAccount(String anchorAccountId,String apiEnvironment);
    AccountDetails fetchSubAccount(String anchorSubAccountId,String apiEnvironment);
    AccountDetails fetchReservedAccount(String anchorReservedAccountId,String apiEnvironment);
    void freezeDepositAccount(String anchorAccountId,String reason,String apiEnvironment);
    void unfreezeDepositAccount(String anchorAccountId,String apiEnvironment);
    record DepositAccountResult(String anchorAccountId,String status){}
    record SubAccountResult(String anchorSubAccountId,String anchorVirtualNubanId,String status){}
    record ReservedAccountResult(String anchorReservedAccountId,String anchorCustomerId,String status){}
    record ReservedAccountCustomer(String type,String referenceId,String providerCustomerId,String fullName,String email,String bvn){
        public ReservedAccountCustomer(String type,String referenceId,String fullName,String email,String bvn){
            this(type,referenceId,null,fullName,email,bvn);}}
    record AccountDetails(String resourceId,String accountName,String accountNumber,String maskedAccountNumber,
                          String bankName,String bankCode,String currency,String status){}
}
