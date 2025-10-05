package com.kopi.belimang.auth.services;

import com.kopi.belimang.auth.core.jwt.JwtUtil;
import com.kopi.belimang.auth.core.jwt.UserClaim;
import com.kopi.belimang.auth.dto.TokenDto;
import com.kopi.belimang.auth.exceptions.CredentialInvalidException;
import com.kopi.belimang.auth.exceptions.DuplicateCredentialException;
import com.kopi.belimang.core.entities.User;
import com.kopi.belimang.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public TokenDto loginAsAdmin(String username, String password) {
        User admin = userRepository.findAdminByUsername(username);

        if ( admin == null ) {
            throw new CredentialInvalidException("Credential is incorrect");
        }

        String token = jwtUtil.generateToken(new UserClaim(admin.getId()));

        return new TokenDto(token);
    }

    @Override
    public TokenDto registerAsAdmin(String username, String password, String email) {
        if ( userRepository.findAdminByEmail(email) != null ) {
            throw new DuplicateCredentialException("Email already used");
        }

        if ( !CollectionUtils.isEmpty(userRepository.findByUsername(username)) ) {
            throw new DuplicateCredentialException("Username already taken");
        }

        User user = User.builder()
                .isAdmin(true)
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(new UserClaim(user.getId()));

        return new TokenDto(token);
    }

    @Override
    public TokenDto loginAsUser(String username, String password) {
        User admin = userRepository.findNonadminByUsername(username);

        if ( admin == null ) {
            throw new CredentialInvalidException("Credential is incorrect");
        }

        String token = jwtUtil.generateToken(new UserClaim(admin.getId()));

        return new TokenDto(token);
    }

    @Override
    public TokenDto registerAsUser(String username, String password, String email) {
        if ( userRepository.findNonadminByEmail(email) != null ) {
            throw new DuplicateCredentialException("Email already used");
        }

        if ( !CollectionUtils.isEmpty(userRepository.findByUsername(username)) ) {
            throw new DuplicateCredentialException("Username already taken");
        }

        User user = User.builder()
                .isAdmin(false)
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(new UserClaim(user.getId()));

        return new TokenDto(token);
    }
}
