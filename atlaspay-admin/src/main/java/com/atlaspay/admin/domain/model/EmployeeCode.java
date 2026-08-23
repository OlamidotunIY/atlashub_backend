package com.atlaspay.admin.domain.model;

import java.security.SecureRandom;

public record EmployeeCode(String rawCode) {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static EmployeeCode generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return new EmployeeCode(sb.toString());
    }

    public String formatted() {
        return "ATL-" + rawCode;
    }
}
