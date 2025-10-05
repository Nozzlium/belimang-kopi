package com.kopi.belimang.auth.core.guard;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GuardRegistry {
    private Map<String, Guard> registry;

    public GuardRegistry() {
        registry = new HashMap<>();
    }

    public void put(String key, Guard value) {
        registry.put(key, value);
    }

    public Map<String, Guard> getRegistry() {
        return registry;
    }
}
