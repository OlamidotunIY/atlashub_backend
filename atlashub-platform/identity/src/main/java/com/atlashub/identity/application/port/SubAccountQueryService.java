package com.atlashub.identity.application.port;

import com.atlashub.identity.application.dto.SubAccountDto;
import com.atlashub.shared.util.PageResult;

import java.util.Optional;

public interface SubAccountQueryService {
    Optional<SubAccountDto> findById(Long merchantId, Long subAccountId);
    PageResult<SubAccountDto> findAllByMerchantId(Long merchantId, int page, int size);
}
