package com.appointease.domain.model;

public record AppointmentId(String value) {
    public AppointmentId { value = Validation.text(value, "appointment ID"); }
}
