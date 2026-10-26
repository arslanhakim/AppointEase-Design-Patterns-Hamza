package com.appointease.domain.mode;
import com.appointease.domain.model.AppointmentMode;
/** Immutable product bound to one factory instance. */
public sealed interface MeetingDetails permits OnlineModeFactory.OnlineMeetingDetails, InPersonModeFactory.InPersonMeetingDetails {
    AppointmentMode mode();
    Object familyIdentity();
    String text();
}
