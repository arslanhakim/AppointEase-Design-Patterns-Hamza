package com.appointease.domain.mode;
/** Abstract Factory: related products independent of appointment kind. */
public interface AppointmentModeFactory {
    MeetingDetails createMeetingDetails();
    ParticipationInstructions createParticipationInstructions();
}
