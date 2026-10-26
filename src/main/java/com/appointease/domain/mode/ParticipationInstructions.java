package com.appointease.domain.mode;
import com.appointease.domain.model.AppointmentMode;
/** Immutable product bound to one factory instance. */
public sealed interface ParticipationInstructions permits OnlineModeFactory.OnlineParticipationInstructions, InPersonModeFactory.InPersonParticipationInstructions {
    AppointmentMode mode();
    Object familyIdentity();
    String text();
}
