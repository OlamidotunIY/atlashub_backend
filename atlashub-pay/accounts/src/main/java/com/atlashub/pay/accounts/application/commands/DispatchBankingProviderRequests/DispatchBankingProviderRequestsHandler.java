package com.atlashub.pay.accounts.application.commands.DispatchBankingProviderRequests;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort;
import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort.ReservedAccountCustomer;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.RequestStatus;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DispatchBankingProviderRequestsHandler extends Command<DispatchBankingProviderRequestsCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(DispatchBankingProviderRequestsHandler.class);
    private final BankingProviderRequestRepository requestRepository;
    private final BusinessDepositAccountRepository depositRepository;
    private final BusinessSubAccountRepository subAccountRepository;
    private final ReservedAccountRepository reservedRepository;
    private final OrganizationBankingProfileRepository profileRepository;
    private final AnchorBankingPort anchorBankingPort;

    public DispatchBankingProviderRequestsHandler(BankingProviderRequestRepository requestRepository, BusinessDepositAccountRepository depositRepository, BusinessSubAccountRepository subAccountRepository, ReservedAccountRepository reservedRepository, OrganizationBankingProfileRepository profileRepository, AnchorBankingPort anchorBankingPort) {
        this.requestRepository = requestRepository;
        this.depositRepository = depositRepository;
        this.subAccountRepository = subAccountRepository;
        this.reservedRepository = reservedRepository;
        this.profileRepository = profileRepository;
        this.anchorBankingPort = anchorBankingPort;
    }

    @Override
    public Void execute(DispatchBankingProviderRequestsCommand command) {
        requestRepository.findPending(command.batchSize()).forEach(this::dispatch);
        return null;
    }

    private void dispatch(BankingProviderRequest request) {
        try {
            switch (request.getRequestType()) {
                case DEPOSIT -> dispatchDeposit(request);
                case SUB_ACCOUNT -> dispatchSubAccount(request);
                case RESERVED_ACCOUNT -> dispatchReserved(request);
                case FREEZE_DEPOSIT -> freezeDeposit(request);
                case UNFREEZE_DEPOSIT -> unfreezeDeposit(request);
            }
            request.complete();
        } catch (RuntimeException error) {
            log.warn("Anchor request {} failed: {}", request.getRequestReference(), error.getMessage());
            request.fail(error.getMessage());
            if (request.getStatus() == RequestStatus.FAILED) {
                markTerminalFailure(request);
            }
        }
        requestRepository.save(request);
    }

    private void dispatchDeposit(BankingProviderRequest request) {
        BusinessDepositAccount account = depositRepository.findById(request.getAggregateId()).orElseThrow();
        AnchorBankingPort.DepositAccountResult result = anchorBankingPort.createBusinessDepositAccount(request.getAnchorCustomerId(), "CURRENT", request.getRequestReference(), request.getApiEnvironment());
        account.markSubmitted(result.anchorAccountId());
        depositRepository.save(account);
    }

    private void dispatchSubAccount(BankingProviderRequest request) {
        BusinessSubAccount account = subAccountRepository.findById(request.getAggregateId()).orElseThrow();
        AnchorBankingPort.SubAccountResult result = anchorBankingPort.createBusinessSubAccount(request.getAnchorCustomerId(), request.getParentOrPayoutAccountId(), true, request.getRequestReference(), request.getApiEnvironment());
        account.markSubmitted(result.anchorSubAccountId(), result.anchorVirtualNubanId());
        subAccountRepository.save(account);
    }

    private void dispatchReserved(BankingProviderRequest request) {
        ReservedAccount account = reservedRepository.findById(request.getAggregateId()).orElseThrow();
        ReservedAccountCustomer customer = new ReservedAccountCustomer(
                request.getCustomerType(), request.getCustomerReferenceId(), request.getAnchorCustomerId(),
                request.getCustomerFullName(), request.getCustomerEmail(), request.getCustomerBvn());
        AnchorBankingPort.ReservedAccountResult result = anchorBankingPort.createReservedAccount(customer, request.getProvider(), request.getParentOrPayoutAccountId(), request.getRequestReference(), request.getApiEnvironment());
        account.markSubmitted(result.anchorReservedAccountId(), result.anchorCustomerId());
        reservedRepository.save(account);
    }

    private void freezeDeposit(BankingProviderRequest request) {
        BusinessDepositAccount account = depositRepository.findById(request.getAggregateId()).orElseThrow();
        anchorBankingPort.freezeDepositAccount(account.getAnchorAccountId(), request.getOperationReason(),
                request.getApiEnvironment());
        account.freeze(request.getOperationReason());
        depositRepository.save(account);
    }

    private void unfreezeDeposit(BankingProviderRequest request) {
        BusinessDepositAccount account = depositRepository.findById(request.getAggregateId()).orElseThrow();
        anchorBankingPort.unfreezeDepositAccount(account.getAnchorAccountId(), request.getApiEnvironment());
        account.reactivate();
        depositRepository.save(account);
        profileRepository.findById(account.getBankingProfileId()).ifPresent(profile -> {
            if (profile.getActiveRestrictions().isEmpty()) profile.reactivate();
            profileRepository.save(profile);
        });
    }

    private void markTerminalFailure(BankingProviderRequest request) {
        switch (request.getRequestType()) {
            case DEPOSIT -> depositRepository.findById(request.getAggregateId()).ifPresent(account -> {
                account.fail("Provider request failed after retry limit");
                depositRepository.save(account);
                profileRepository.findById(account.getBankingProfileId()).ifPresent(profile -> {
                    profile.fail("DEPOSIT_ACCOUNT_FAILED", "Provider request failed after retry limit");
                    profileRepository.save(profile);
                });
            });
            case SUB_ACCOUNT -> subAccountRepository.findById(request.getAggregateId()).ifPresent(account -> {
                account.fail("Provider request failed after retry limit");
                subAccountRepository.save(account);
                profileRepository.findById(account.getBankingProfileId()).ifPresent(profile -> {
                    profile.markPartiallyProvisioned("SUB_ACCOUNT_FAILED", "Provider request failed after retry limit");
                    profileRepository.save(profile);
                });
            });
            case RESERVED_ACCOUNT -> reservedRepository.findById(request.getAggregateId()).ifPresent(account -> {
                account.fail("Provider request failed after retry limit");
                reservedRepository.save(account);
            });
            case FREEZE_DEPOSIT, UNFREEZE_DEPOSIT -> { }
        }
    }
}
