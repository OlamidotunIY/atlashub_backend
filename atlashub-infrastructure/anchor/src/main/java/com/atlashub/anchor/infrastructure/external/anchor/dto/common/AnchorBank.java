package com.atlashub.anchor.infrastructure.external.anchor.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorBank(String id, String provider, String name, String cbnCode, String nipCode) {
}
