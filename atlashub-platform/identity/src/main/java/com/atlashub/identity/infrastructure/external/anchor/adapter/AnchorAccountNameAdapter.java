package com.atlashub.identity.infrastructure.external.anchor.adapter;

import com.atlashub.identity.application.port.AccountNameResolutionPort;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AnchorAccountNameAdapter implements AccountNameResolutionPort {

    private final RestTemplate restTemplate;

    public AnchorAccountNameAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String resolve(String bankCode, String accountNumber) {
        if (bankCode == null || accountNumber == null || accountNumber.length() != 10) {
            throw new NotFoundException(IdentityErrorCode.BANK_ACCOUNT_NOT_FOUND, "Bank account not found in NIP directory");
        }
        
        // TODO: Make actual HTTP call to Anchor BaaS API
        // For now, return a placeholder string representing the resolved name
        return "JOHN DOE (Anchor Resolved)";
    }
}
