package com.appointease.domain.mode;
import com.appointease.domain.model.Validation;
import com.appointease.domain.exception.DomainValidationException;
/** Same-mode products from different sessions are also incompatible. */
public record AppointmentModeDetails(MeetingDetails meeting, ParticipationInstructions instructions) {
    public AppointmentModeDetails {
        Validation.required(meeting, "meeting details");
        Validation.required(instructions, "participation instructions");
        if (meeting.mode() != instructions.mode() || meeting.familyIdentity() != instructions.familyIdentity()) {
            throw new DomainValidationException("Mode products must belong to the same family instance");
        }
    }
}
