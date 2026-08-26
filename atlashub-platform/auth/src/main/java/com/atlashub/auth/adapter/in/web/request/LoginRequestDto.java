package com.atlashub.auth.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Identifier is required")
    private String identifier;
    
    @NotBlank(message = "Password is required")
    private String password;
}
