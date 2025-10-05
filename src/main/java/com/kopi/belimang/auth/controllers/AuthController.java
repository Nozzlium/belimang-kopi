package com.kopi.belimang.auth.controllers;

import com.kopi.belimang.auth.dto.LoginCredentialDto;
import com.kopi.belimang.auth.dto.RegisterCredentialDto;
import com.kopi.belimang.auth.dto.TokenDto;
import com.kopi.belimang.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/admin/login")
    ResponseEntity<TokenDto> loginAdmin(@Valid @RequestBody LoginCredentialDto credential) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.loginAsAdmin(credential.getUsername(), credential.getPassword()));
    }

    @PostMapping("/user/login")
    ResponseEntity<TokenDto> loginUser(@Valid @RequestBody LoginCredentialDto credential) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.loginAsUser(credential.getUsername(), credential.getPassword()));
    }

    @PostMapping("/admin/register")
    ResponseEntity<TokenDto> registerAdmin(@Valid @RequestBody RegisterCredentialDto credential) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerAsAdmin(credential.getUsername(), credential.getPassword(), credential.getEmail()));
    }

    @PostMapping("/user/register")
    ResponseEntity<TokenDto> registerUser(@Valid @RequestBody RegisterCredentialDto credential) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerAsUser(credential.getUsername(), credential.getPassword(), credential.getEmail()));
    }
}

