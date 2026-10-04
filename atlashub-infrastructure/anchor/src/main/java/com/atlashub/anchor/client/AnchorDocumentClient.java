package com.atlashub.anchor.client;

import com.atlashub.anchor.dto.common.AnchorCollectionResponse;
import com.atlashub.anchor.dto.document.AnchorDocumentResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;

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
