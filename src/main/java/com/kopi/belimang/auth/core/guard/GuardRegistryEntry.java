package com.kopi.belimang.auth.core.guard;

public record GuardRegistryEntry(
        String url,
        String httpMethod,
        String[] acceptedRoles
) {
}
