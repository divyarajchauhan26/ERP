package edu.univ.erp.domain;

public class Settings {
    private String key;
    private String value;

    public Settings(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // --- Getters ---
    public String getKey() { return key; }
    public String getValue() { return value; }
}