package com.kopi.belimang.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterCredentialDto {
    @Size(min = 5, max = 30)
    @NotNull
    private String username;

    @Size(min = 5, max = 30)
    @NotNull
    private String password;

    @NotNull
    @Email
    private String email;
}
