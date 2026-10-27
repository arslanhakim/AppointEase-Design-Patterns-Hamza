package com.appointease.presentation.desktop;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarKeyboardTest {
    @ParameterizedTest @CsvSource({
        "2026-12-31,RIGHT,2027-01-01", "2027-01-01,LEFT,2026-12-31",
        "2026-03-02,UP,2026-02-23", "2026-03-29,DOWN,2026-04-05",
        "2026-10-28,HOME,2026-10-26", "2026-10-28,END,2026-11-01",
        "2026-01-31,PAGE_DOWN,2026-02-28", "2028-03-31,PAGE_UP,2028-02-29",
        "2026-12-15,PAGE_DOWN,2027-01-15", "2026-10-28,TAB,2026-10-28",
        "2026-10-28,ENTER,2026-10-28", "2026-10-28,SPACE,2026-10-28"
    })
    void keyboardTraversesDatesWithoutChangingActivationOrTab(String start, String key, String expected) {
        assertEquals(LocalDate.parse(expected), CalendarKeyboard.move(LocalDate.parse(start), KeyCode.valueOf(key)));
    }
}
