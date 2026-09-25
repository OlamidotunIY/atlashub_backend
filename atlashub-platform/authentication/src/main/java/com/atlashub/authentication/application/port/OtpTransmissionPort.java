package com.atlashub.authentication.application.port;

public interface OtpTransmissionPort {
    void storeForTransmission(String correlationId, String rawCode);

    String retrieveForTransmission(String correlationId);
}
