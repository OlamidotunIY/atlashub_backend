package com.atlashub.auth.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeTemporaryPasswordRequestDto {
    @NotBlank(message = "Identifier is required")
    private String identifier;
    
    @NotBlank(message = "Old password is required")
    private String oldPassword;
    
    @NotBlank(message = "New password is required")
    private String newPassword;
}
