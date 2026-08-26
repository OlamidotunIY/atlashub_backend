package com.atlashub.identity.application.port;

import com.atlashub.identity.application.dto.CustomerDto;
import com.atlashub.shared.util.PageResult;

import java.util.Optional;

public interface CustomerQueryService {
    Optional<CustomerDto> findById(Long merchantId, Long customerId);
    PageResult<CustomerDto> findAllByMerchantId(Long merchantId, int page, int size, String emailFilter);
}
