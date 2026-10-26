package com.appointease.domain.model;

public record ProviderId(String value) {
    public ProviderId { value = Validation.text(value, "provider ID"); }
}
