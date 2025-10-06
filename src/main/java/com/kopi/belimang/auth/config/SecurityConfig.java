package com.kopi.belimang.auth.config;


import com.kopi.belimang.auth.core.AuthenticationEntryPointImpl;
import com.kopi.belimang.auth.core.guard.GuardRegistry;
import com.kopi.belimang.auth.core.CustomUserDetailService;
import com.kopi.belimang.auth.core.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final GuardRegistry guardRegistry;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationEntryPointImpl customEntryPoint;

    public SecurityConfig(GuardRegistry guardRegistry, CustomUserDetailService customUserDetailService, AuthenticationEntryPointImpl customEntryPoint) {
        this.guardRegistry = guardRegistry;
        this.customUserDetailService = customUserDetailService;
        this.customEntryPoint = customEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity httpSecurity, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        httpSecurity
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> {
                    guardRegistry.getRegistry().forEach((entry) -> {
                        registry.requestMatchers(entry.httpMethod(), entry.url()).hasAnyAuthority(entry.acceptedRoles());
                    });
                    registry.requestMatchers("admin/register").permitAll();
                    registry.requestMatchers("users/register").permitAll();
                    registry.requestMatchers("admin/login").permitAll();
                    registry.requestMatchers("users/login").permitAll();
                })
                .authenticationProvider(daoAuthenticationProvider())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailService);
        provider.setPasswordEncoder(getPasswordEncoder());

        return provider;
    }
}
