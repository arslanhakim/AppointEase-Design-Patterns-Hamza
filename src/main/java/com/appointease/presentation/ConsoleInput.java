package com.appointease.presentation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;

/** Syntax parsing only. EOF unwinds any unfinished interaction without a write. */
final class ConsoleInput {
    static final class EndOfInput extends RuntimeException { }
    private final BufferedReader reader;
    private final PrintWriter out;
    ConsoleInput(InputStream input, PrintWriter out) {
        reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.out = out;
    }
    String read(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) throw new EndOfInput();
            if (line.isBlank()) throw new IllegalArgumentException("Input is required; please retry from the menu.");
            return line.strip();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    boolean yes(String prompt) {
        return switch (read(prompt + " [y/n]").toLowerCase(java.util.Locale.ROOT)) {
            case "y", "yes" -> true;
            case "n", "no" -> false;
            default -> throw new IllegalArgumentException("Enter y or n.");
        };
    }
    LocalDateTime dateTime() {
        var date = LocalDate.parse(read("Date [YYYY-MM-DD]"), DateTimeFormatter.ISO_LOCAL_DATE);
        var time = LocalTime.parse(read("Time [HH:mm]"),
                DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT));
        return LocalDateTime.of(date, time);
    }
}
