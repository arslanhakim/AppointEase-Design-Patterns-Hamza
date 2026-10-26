package com.appointease.domain.mode;
import com.appointease.domain.model.AppointmentMode;
import com.appointease.domain.model.Validation;
/** Offline immutable family; no external service. */
public final class InPersonModeFactory implements AppointmentModeFactory {
    private final Object identity = new Object();
    private final String location;
    public InPersonModeFactory(String location) { this.location = Validation.text(location, "location"); }
    @Override public MeetingDetails createMeetingDetails() {
        return new InPersonMeetingDetails(identity, "Location: " + location);
    }
    @Override public ParticipationInstructions createParticipationInstructions() {
        return new InPersonParticipationInstructions(identity, "Check in at reception: " + location);
    }
    static record InPersonMeetingDetails(Object familyIdentity, String text) implements MeetingDetails {
        @Override public AppointmentMode mode() { return AppointmentMode.IN_PERSON; }
    }
    static record InPersonParticipationInstructions(Object familyIdentity, String text) implements ParticipationInstructions {
        @Override public AppointmentMode mode() { return AppointmentMode.IN_PERSON; }
    }
}
