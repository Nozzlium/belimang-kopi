package com.kopi.belimang.auth.services;

import com.kopi.belimang.auth.dto.TokenDto;

public interface AuthService {
    TokenDto loginAsAdmin(String username, String password);
    TokenDto registerAsAdmin(String username, String password, String email);
    TokenDto loginAsUser(String username, String password);
    TokenDto registerAsUser(String username, String password, String email);
}
