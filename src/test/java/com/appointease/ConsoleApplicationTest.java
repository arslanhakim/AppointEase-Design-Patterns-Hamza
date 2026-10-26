package com.appointease;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleApplicationTest {
    private String run(String script) {
        var output = new ByteArrayOutputStream();
        Main.create(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), output,
                Clock.fixed(Instant.parse("2026-10-26T07:00:00Z"), ZoneOffset.UTC), ZoneId.of("Europe/Vilnius")).run();
        return output.toString(StandardCharsets.UTF_8);
    }
    private String booking(String at, String policy, String confirmation) {
        return "4\n1\nprovider-health-001\n1\n1\n2026-10-27\n" + at + "\n1\ny\ny\n" + policy + "\n" + confirmation;
    }
    @Test void browseCatalogAndExit() {
        String out = run("1\n2\n0\n");
        for (String expected : new String[]{"HEALTHCARE", "TUTORING", "CONSULTING", "PERSONAL_SERVICES",
                "EUR 40.00", "EUR 25.00", "EUR 60.00", "EUR 20.00", "MONDAY", "Goodbye."}) assertTrue(out.contains(expected), expected);
        assertFalse(out.contains("End of input"));
    }
    @Test void bookingConfirmationRescheduleCancellationAndHistory() {
        String out = run(booking("10:00", "1", "y\n") + "7\n@1\n8\n@1\n2026-10-27\n11:00\n2\ny\n6\n@1\n9\n@1\n5\n1\n11\n0\n");
        for (String expected : new String[]{"Booked successfully", "EUR 52.00", "Mode: ONLINE", "Status: PENDING",
                "Status: CONFIRMED", "RESCHEDULED", "Status: CANCELLED", "Notifications (simulated)", "Audit history", "Appointment history"})
            assertTrue(out.contains(expected), expected);
        assertFalse(out.contains("Unable to complete"));
    }
    @Test void overlapRejectedAdjacentAcceptedAndBufferReschedulePreservesSlot() {
        String out = run(booking("10:00", "1", "y\n") + booking("10:00", "1", "")
                + booking("10:30", "1", "y\n") + "8\n@2\n2026-10-27\n10:30\n2\ny\n6\n@2\n0\n");
        assertEquals(2, out.split("Booked successfully", -1).length - 1);
        assertTrue(out.contains("Provider slot is unavailable"));
        assertTrue(out.contains("Unable to complete request: Provider slot is unavailable"));
        assertTrue(out.contains("Time: 2026-10-27 10:30 +02:00 -> 2026-10-27 11:00 +02:00"));
        assertFalse(out.contains("RESCHEDULED"));
    }
    @Test void cancellationReleasesSlot() {
        String out = run(booking("10:00", "2", "y\n") + "9\n@1\n" + booking("10:00", "2", "y\n") + "0\n");
        assertEquals(2, out.split("Booked successfully", -1).length - 1);
        assertFalse(out.contains("unavailable"));
    }
    @Test void completeElapsedSeedAndRejectTerminalTransition() {
        String out = run("10\nseed-ready-001\n7\nseed-ready-001\n6\nseed-ready-001\n11\n0\n");
        assertTrue(out.contains("Status: COMPLETED"));
        assertTrue(out.contains("Unable to complete request"));
        assertTrue(out.contains("COMPLETED"));
        assertTrue(out.contains("Revision 3"));
    }
    @Test void completionBeforeEndRejected() {
        String out = run(booking("10:00", "1", "y\n") + "7\n@1\n10\n@1\n6\n@1\n0\n");
        assertTrue(out.contains("Unable to complete request"));
        assertFalse(out.contains("Status: COMPLETED"));
        assertTrue(out.contains("Status: CONFIRMED"));
    }
    @Test void inPersonServiceUsesCorrectFactoryAndNoExtras() {
        String out = run("4\n2\nprovider-salon-001\n2\n1\n2026-10-27\n10:00\n2\nn\nn\n1\ny\n0\n");
        assertTrue(out.contains("Booked successfully"));
        assertTrue(out.contains("Type: SERVICE"));
        assertTrue(out.contains("Mode: IN_PERSON"));
        assertTrue(out.contains("Total fee: EUR 20.00"));
        assertTrue(out.contains("Check in at reception"));
    }
    @Test void unsupportedModeRejectedByDomain() {
        String out = run("4\n1\nprovider-salon-001\n2\n1\n2026-10-27\n10:00\n1\nn\nn\n1\n0\n");
        assertTrue(out.contains("Unable to complete request"));
        assertFalse(out.contains("Booked successfully"));
    }
    @Test void availabilityDoesNotBookAndDeclineDoesNotBook() {
        String out = run(booking("10:00", "1", "n\n") + booking("10:00", "1", "").replaceFirst("4", "3") + "0\n");
        assertTrue(out.contains("Booking discarded"));
        assertTrue(out.contains("Slot available"));
        assertFalse(out.contains("Booked successfully"));
    }
    @ParameterizedTest
    @ValueSource(strings = {"", "4\n", "4\n1\n", "4\n1\nprovider-health-001\n1\n1\n2026-10-27\n",
            "4\n1\nprovider-health-001\n1\n1\n2026-10-27\n10:00\n1\nn\nn\n1\n"})
    void eofAtEveryInteractionStageExitsWithoutBooking(String script) {
        String out = run(script);
        assertTrue(out.contains("End of input"));
        assertTrue(out.endsWith("Goodbye." + System.lineSeparator()));
        assertFalse(out.contains("Booked successfully"));
    }
    @ParameterizedTest
    @ValueSource(strings = {"\n", "abc\n", "99\n", "6\nmissing\n", "6\n@99\n", "4\nunknown\n",
            "4\n1\nmissing-provider\n", "4\n1\nprovider-health-001\n1\nmissing-service\n",
            "4\n1\nprovider-health-001\n1\n1\n2026-02-30\n",
            "4\n1\nprovider-health-001\n1\n1\n2026-10-27\n25:00\n"})
    void invalidInputRecoversToMenu(String bad) {
        String out = run(bad + "2\n0\n");
        assertTrue(out.contains("Invalid menu option") || out.contains("Unable to complete request"));
        assertTrue(out.contains("EUR 60.00"));
        assertTrue(out.contains("Goodbye."));
    }
    @Test void independentCompositionRootsDoNotShareState() {
        assertTrue(run(booking("10:00", "1", "y\n") + "0\n").contains("Booked successfully"));
        assertFalse(run("5\n1\n0\n").contains("PENDING"));
    }
    @Test void configuredZoneIsUsed() {
        var output = new ByteArrayOutputStream();
        Main.create(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)), output,
                Clock.fixed(Instant.parse("2026-10-26T07:00:00Z"), ZoneOffset.UTC), ZoneId.of("UTC")).run();
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Zone: UTC"));
    }
}
