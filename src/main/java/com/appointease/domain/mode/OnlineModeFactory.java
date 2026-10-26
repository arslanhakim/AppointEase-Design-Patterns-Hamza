package com.appointease.domain.mode;
import com.appointease.domain.model.AppointmentMode;
import com.appointease.domain.model.Validation;
/** Offline immutable family; no external service. */
public final class OnlineModeFactory implements AppointmentModeFactory {
    private final Object identity = new Object();
    private final String meetingCode;
    public OnlineModeFactory(String meetingCode) { this.meetingCode = Validation.text(meetingCode, "meetingCode"); }
    @Override public MeetingDetails createMeetingDetails() {
        return new OnlineMeetingDetails(identity, "Simulated meeting: " + meetingCode);
    }
    @Override public ParticipationInstructions createParticipationInstructions() {
        return new OnlineParticipationInstructions(identity, "Join simulated meeting " + meetingCode);
    }
    static record OnlineMeetingDetails(Object familyIdentity, String text) implements MeetingDetails {
        @Override public AppointmentMode mode() { return AppointmentMode.ONLINE; }
    }
    static record OnlineParticipationInstructions(Object familyIdentity, String text) implements ParticipationInstructions {
        @Override public AppointmentMode mode() { return AppointmentMode.ONLINE; }
    }
}
