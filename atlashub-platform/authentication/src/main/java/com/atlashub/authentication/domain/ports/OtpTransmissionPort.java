package com.atlashub.authentication.domain.services;

public interface OtpTransmissionPort {
    void storeForTransmission(String correlationId, String rawCode);

    String retrieveForTransmission(String correlationId);
}
