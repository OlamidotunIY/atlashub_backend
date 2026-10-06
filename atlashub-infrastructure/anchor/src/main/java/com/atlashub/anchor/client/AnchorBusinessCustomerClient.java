package com.atlashub.anchor.client;

import com.atlashub.anchor.dto.common.*;
import com.atlashub.anchor.dto.customer.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.*;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface AnchorBusinessCustomerClient {
    @PostExchange("/api/v1/customers")
    AnchorResponse<BusinessCustomerResource> create(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     @RequestBody AnchorRequest<CreateBusinessCustomerData> request);
    @GetExchange("/api/v1/customers/{customerId}")
    AnchorResponse<BusinessCustomerResource> fetch(@PathVariable String customerId);
    @PostExchange("/api/v1/customers/{customerId}/verification/business")
    void triggerVerification(@PathVariable String customerId);
}
