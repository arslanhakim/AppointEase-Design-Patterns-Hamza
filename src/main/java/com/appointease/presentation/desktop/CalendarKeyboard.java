package com.appointease.presentation.desktop;

import java.time.LocalDate;
import javafx.scene.input.KeyCode;

/** Date-only keyboard movement; appointment instants and DST resolution stay untouched. */
final class CalendarKeyboard {
    private CalendarKeyboard() { }
    static LocalDate move(LocalDate day, KeyCode key) {
        return switch (key) {
            case LEFT -> day.minusDays(1);
            case RIGHT -> day.plusDays(1);
            case UP -> day.minusDays(7);
            case DOWN -> day.plusDays(7);
            case HOME -> day.minusDays(day.getDayOfWeek().getValue() - 1);
            case END -> day.plusDays(7 - day.getDayOfWeek().getValue());
            case PAGE_UP -> day.minusMonths(1);
            case PAGE_DOWN -> day.plusMonths(1);
            default -> day;
        };
    }
}
