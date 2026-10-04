package com.atlashub.anchor.dto.document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorDocumentResource(String id, String type, Attributes attributes) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(String description, String type, String status) {}
}
