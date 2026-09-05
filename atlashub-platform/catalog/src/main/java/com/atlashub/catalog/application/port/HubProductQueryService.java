package com.atlashub.catalog.application.port;

import com.atlashub.catalog.application.result.HubProductDetailsResult;

public interface HubProductQueryService {
    HubProductDetailsResult getHubProductDetails(Long productId);
}
