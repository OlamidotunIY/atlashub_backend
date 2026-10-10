package com.atlashub.anchor.infrastructure.external.anchor.dto.common;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorCollectionResponse<T>(@JsonProperty("data") List<T> data) {}
