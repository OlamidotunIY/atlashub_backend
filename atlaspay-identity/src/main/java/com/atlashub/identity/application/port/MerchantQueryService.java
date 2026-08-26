package com.atlashub.identity.application.port;

import com.atlashub.identity.application.dto.MerchantProfileDto;

import java.util.Optional;

public interface MerchantQueryService {
    Optional<MerchantProfileDto> findProfileById(Long merchantId);
}
