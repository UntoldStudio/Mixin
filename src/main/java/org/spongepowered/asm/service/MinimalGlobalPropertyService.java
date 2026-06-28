package org.spongepowered.asm.service;

import java.util.HashMap;
import java.util.Map;

public class MinimalGlobalPropertyService implements IGlobalPropertyService {

    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public IPropertyKey resolveKey(String key) {
        return new MinimalPropertyKey(key);
    }

    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T) properties.get(key.toString());
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        properties.put(key.toString(), value);
    }

    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        T value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object value = getProperty(key);
        return value != null ? value.toString() : defaultValue;
    }

    private static class MinimalPropertyKey implements IPropertyKey {
        private final String key;
        MinimalPropertyKey(String key) { this.key = key; }
        @Override
        public String toString() { return key; }
    }
}