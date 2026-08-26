package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.dto.MerchantProfileDto;
import com.atlashub.identity.application.port.MerchantQueryService;
import com.atlashub.identity.application.query.GetMerchantProfileQuery;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;

@Service
public class GetMerchantProfileUseCase extends BaseUseCase<GetMerchantProfileQuery, MerchantProfileDto> {
    private static final Logger log = LoggerFactory.getLogger(GetMerchantProfileUseCase.class);


    private final MerchantQueryService queryService;

    public GetMerchantProfileUseCase(MerchantQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public MerchantProfileDto execute(GetMerchantProfileQuery query) {
        log.info("Executing GetMerchantProfileUseCase");

        return queryService.findProfileById(query.merchantId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.MERCHANT_NOT_FOUND, "Merchant not found"));
    }
}


