package com.atlashub.anchor.infrastructure.external.anchor.client;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorCollectionResponse;
import com.atlashub.anchor.infrastructure.external.anchor.dto.document.AnchorDocumentResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface AnchorDocumentClient {
    @GetExchange("/api/v1/documents")
    AnchorCollectionResponse<AnchorDocumentResource> preview(@RequestParam String registrationType,
                                                              @RequestParam String registrationDate);
    @GetExchange("/api/v1/documents/{customerId}")
    AnchorCollectionResponse<AnchorDocumentResource> listForCustomer(@PathVariable String customerId);
    @PostExchange(value = "/api/v1/documents/upload-document/{customerId}/{documentId}",
            contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    void upload(@PathVariable String customerId, @PathVariable String documentId,
                @RequestPart(name = "textData", required = false) String textData,
                @RequestPart(name = "fileData", required = false) Resource fileData);
}
