package com.kopi.belimang.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginCredentialDto {
    @Size(min = 5, max = 30)
    @NotNull
    private String username;

    @Size(min = 5, max = 30)
    @NotNull
    private String password;
}
