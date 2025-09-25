package com.kopi.belimang.auth;

import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class BeanRegistry {
    private Map<String, Guard> registry;

    public BeanRegistry() {
        registry = new HashMap<>();
    }

    public void put(String key, Guard value) {
        registry.put(key, value);
    }

    public Map<String, Guard> getRegistry() {
        return registry;
    }
}
