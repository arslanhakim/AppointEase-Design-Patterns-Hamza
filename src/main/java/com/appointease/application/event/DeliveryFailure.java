package com.appointease.application.event;
/** Failure is visible without changing the committed result. */
public record DeliveryFailure(String eventId, String consumer, RuntimeException cause) { }
