package com.atlashub.pay.accounts.application.commands.IssueVirtualAccount;

import com.atlashub.pay.accounts.domain.entities.VirtualAccount;
import com.atlashub.pay.accounts.domain.exceptions.VirtualAccountAlreadyExistsException;
import com.atlashub.pay.accounts.domain.ports.AnchorPort;
import com.atlashub.pay.accounts.domain.repositories.VirtualAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.OwnerType;
import com.atlashub.pay.accounts.domain.valueobject.VirtualAccountStatus;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Anchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class IssueVirtualAccountHandler extends Command<IssueVirtualAccountCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(IssueVirtualAccountHandler.class);
    private final VirtualAccountRepository accountRepository;
    private final AnchorPort anchorPort;

    public IssueVirtualAccountHandler(VirtualAccountRepository accountRepository, AnchorPort anchorPort) {
        this.accountRepository = accountRepository;
        this.anchorPort = anchorPort;
    }

    @Override
    @Transactional
    public Void execute(IssueVirtualAccountCommand command) {
        log.info("Starting ActivateVirtualAccountHandler");

        Optional<VirtualAccount> existingAccount = accountRepository.findByOrganizationIdAndOwnerType(command.organizationId(), OwnerType.ORGANIZATION);

        if (existingAccount.isPresent()) {
            var status = existingAccount.get().getStatus();

            if (status == VirtualAccountStatus.ACTIVE || status == VirtualAccountStatus.PENDING_ISSUANCE) {
                throw new VirtualAccountAlreadyExistsException("An active or pending org-level virtual account already exists for this organization");
            }
        }

        Anchor.ReserveAccountResponse reserveAccount = anchorPort.issueVirtualAccount(command.anchorCustomerId(), Anchor.CustomerType.BusinessCustomer.name());

        VirtualAccount virtualAccount = VirtualAccount.create(accountRepository.nextIdentity(), command.organizationId(), OwnerType.ORGANIZATION, null, reserveAccount.accountName(), command.currency());

        virtualAccount.activate(reserveAccount.bank().name(), reserveAccount.accountNumber(), reserveAccount.bank().provider(), reserveAccount.accountId());

        accountRepository.save(virtualAccount);

        log.debug("virtual account issued");

        return null;
    }
}
