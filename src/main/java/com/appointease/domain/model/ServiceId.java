package com.appointease.domain.model;

public record ServiceId(String value) {
    public ServiceId { value = Validation.text(value, "service ID"); }
}
