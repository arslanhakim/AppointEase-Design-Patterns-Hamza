package com.appointease;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainSmokeTest {
    @Test
    void welcomeApplicationExitsSuccessfully() throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Process process = new ProcessBuilder(java.toString(), "-cp",
                Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                Main.class.getName()).redirectErrorStream(true).start();
        try {
            process.getOutputStream().close();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Application must exit without input");
            assertEquals(0, process.exitValue());
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(output.startsWith("Welcome to AppointEase!"));
            assertTrue(output.contains("End of input"));
            assertTrue(output.contains("Goodbye."));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
