package com.appointease.presentation.desktop;

import com.appointease.DesktopMain;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Opt-in real desktop smoke check; deliberately outside headless Surefire discovery. */
public final class DesktopVisualCheck {
    private DesktopVisualCheck() { }

    public static void main(String[] args) throws Exception {
        Platform.startup(() -> { });
        Platform.setImplicitExit(false);
        Stage stage = fx(() -> {
            var window = new Stage();
            new DesktopMain().start(window);
            return window;
        });
        try {
            pulse();
            fx(() -> {
                require(stage.isShowing(), "Actual desktop entry point opened a window");
                require(stage.getScene().getRoot().lookupAll(".appointment-row").size() == 4, "Four facade-backed rows rendered");
                var selected = ((DatePicker) stage.getScene().lookup("#overview-date")).getValue();
                ((Button) stage.getScene().lookup("#next-day")).fire();
                require(stage.getScene().getRoot().lookupAll(".appointment-row").isEmpty(), "Date navigation renders an empty day");
                require(stage.getScene().lookup(".empty-state") != null, "Empty-state feedback is visible");
                ((Button) stage.getScene().lookup("#previous-day")).fire();
                require(((DatePicker) stage.getScene().lookup("#overview-date")).getValue().equals(selected), "Previous day restores selection");
                require(stage.getScene().getRoot().lookupAll(".appointment-row").size() == 4, "Rows restored after navigation");
                ((Button) stage.getScene().lookup("#refresh")).fire();
                require(stage.getScene().getRoot().lookupAll(".label").stream().map(Label.class::cast)
                        .anyMatch(label -> label.getText().startsWith("Updated at")), "Refresh feedback is visible");
                var root = (DesktopShell) stage.getScene().getRoot();
                ((Button) stage.getScene().lookup("#navigation-toggle")).fire();
                require(!root.isNavigationExpanded() && root.getLeft() != null, "Navigation collapses to an icon rail");
                ((Button) stage.getScene().lookup("#navigation-toggle")).fire();
                require(root.isNavigationExpanded(), "Navigation expands");
                require(stage.getScene().getRoot().lookupAll(".nav-button").stream().noneMatch(n -> n.isDisabled()),
                        "All six S7B destinations are available");
                stage.setWidth(1024 / stage.getOutputScaleX());
                stage.setHeight(768 / stage.getOutputScaleY());
                stage.toFront();
                ((Button) stage.getScene().lookup("#navigation-toggle")).requestFocus();
                return null;
            });
            pulse();
            fx(() -> {
                require(stage.getScene().lookup(".overview").getStyleClass().contains("compact"), "Compact layout activates");
                new Robot().keyType(KeyCode.TAB);
                return null;
            });
            pulse();
            fx(() -> {
                require(stage.getScene().getFocusOwner() instanceof Button, "Keyboard Tab reaches an enabled button");
                require(!stage.getScene().getFocusOwner().isDisabled(), "Keyboard focus skips unavailable destinations");
                return null;
            });
            System.out.println("DESKTOP CHECK PASSED: window, data, dates, empty state, refresh, navigation, resizing and keyboard focus.");
        } finally {
            fx(() -> { stage.close(); return null; });
            Platform.exit();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        System.out.println("PASS: " + message);
    }

    private static void pulse() throws Exception {
        var completion = new CompletableFuture<Void>();
        Platform.runLater(() -> {
            var pause = new PauseTransition(Duration.millis(350));
            pause.setOnFinished(event -> completion.complete(null));
            pause.play();
        });
        completion.get(10, TimeUnit.SECONDS);
    }

    private static <T> T fx(java.util.concurrent.Callable<T> action) throws Exception {
        var result = new CompletableFuture<T>();
        Platform.runLater(() -> {
            try { result.complete(action.call()); }
            catch (Throwable failure) { result.completeExceptionally(failure); }
        });
        return result.get(10, TimeUnit.SECONDS);
    }
}
