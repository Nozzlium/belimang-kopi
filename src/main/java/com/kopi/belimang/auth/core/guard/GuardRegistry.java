package com.kopi.belimang.auth.core.guard;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class GuardRegistry {
    private Set<GuardRegistryEntry> registry;

    public GuardRegistry() {
        registry = new HashSet<>();
    }

    public void put(String url, String httpMethod, String[] acceptedRoles) {
        registry.add(new GuardRegistryEntry(url, httpMethod, acceptedRoles));
    }

    public Set<GuardRegistryEntry> getRegistry() {
        return registry;
    }
}
