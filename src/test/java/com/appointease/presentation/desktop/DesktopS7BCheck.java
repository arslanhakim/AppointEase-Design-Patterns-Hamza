package com.appointease.presentation.desktop;

import com.appointease.DesktopMain;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.concurrent.*;

/** Opt-in actual Windows UI journeys and raw window captures; not a headless/skipped JUnit test. */
public final class DesktopS7BCheck {
    private final Stage stage;
    private final boolean capture;
    private final boolean polish;
    private int checks;
    private DesktopS7BCheck(Stage stage, boolean capture, boolean polish) { this.stage = stage; this.capture = capture; this.polish = polish; }
    public static void main(String[] args) throws Exception {
        Platform.startup(() -> { });
        Platform.setImplicitExit(false);
        Stage stage = fx(() -> { var s = new Stage(); new DesktopMain().start(s); return s; });
        try { new DesktopS7BCheck(stage, java.util.List.of(args).contains("--capture"), java.util.List.of(args).contains("--s7c")).run(); }
        finally { fx(() -> { stage.close(); return null; }); Platform.exit(); }
    }
    private void run() throws Exception {
        pulse();
        check(() -> require(stage.isShowing(), "Actual DesktopMain window opened"));
        shot("01-overview-desktop", false);
        size(true);
        check(() -> require(!((DesktopShell) stage.getScene().getRoot()).isNavigationExpanded(), "Compact sidebar automatically becomes an icon rail"));
        shot("02-overview-compact", true);
        check(() -> {
            click("navigation-toggle");
            require(((DesktopShell) stage.getScene().getRoot()).isNavigationExpanded(), "Toggle expands compact navigation");
            click("nav-appointments");
            require(!((DesktopShell) stage.getScene().getRoot()).isNavigationExpanded(), "Choosing a compact destination restores the rail");
            require(table().getItems().size() == 5, "Full history includes elapsed seed and four upcoming bookings");
        });
        shot("11-appointments-compact", true);
        size(false);
        shot("03-appointments", false);
        if (polish) {
            check(() -> table().getSelectionModel().select(2));
            pulse();
            check(() -> require(!table().lookupAll(".status").isEmpty(), "Table uses shared text and color status indicators"));
            shot("14-appointments-selected-desktop", false);
            size(true);
            shot("15-appointments-selected-compact", true);
            size(false);
        }
        check(() -> {
            text("appointments-search").setText("desktop-demo-003");
            require(table().getItems().size() == 1, "Search filters actual appointment rows");
            combo("appointments-status").setValue(AppointmentStatus.CONFIRMED);
            require(table().getItems().isEmpty(), "Status and search combine");
            click("appointments-clear");
            require(table().getItems().size() == 5, "Clear filters restores history");
            click("nav-new_appointment");
        });
        shot("04-new-appointment", false);
        if (polish) {
            pulse();
            check(() -> require(visibleInViewport(button("booking-review")), "Desktop booking review action is visible without scrolling"));
        }
        size(true);
        shot("12-new-appointment-compact", true);
        if (polish) {
            check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(1));
            shot("16-new-appointment-pricing-compact", true);
            check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(0));
        }
        check(() -> {
            click("booking-review");
            require(label("booking-feedback").getText().contains("customer"), "Missing customer gives validation feedback");
            combo("booking-customer").getSelectionModel().selectFirst();
            @SuppressWarnings("unchecked") var providers = (ComboBox<ServiceProvider>) node("booking-provider");
            providers.setValue(providers.getItems().stream().filter(p -> p.id().value().contains("health")).findFirst().orElseThrow());
            text("booking-time").setText("09:00");
            click("booking-review");
            require(label("booking-feedback").getText().contains("conflicts"), "Existing reservation is rejected through the real form");
            ((ScrollPane) node("overview-scroll")).setVvalue(1);
        });
        shot("10-conflict-error", true);
        check(() -> {
            text("booking-time").setText("14:00");
            ((CheckBox) node("booking-priority")).setSelected(true);
            ((CheckBox) node("booking-reminder")).setSelected(true);
            click("booking-check");
            require(label("booking-feedback").getText().contains("currently available"), "Availability check is visible");
            if (polish) require(((Label) node("booking-price").lookup(".price-total")).getText().equals("EUR 52.00"),
                    "Emphasized total includes both optional services from the real quote");
            click("booking-review");
            require(node("booking-submit") != null, "Valid form opens review before committing");
            click("booking-edit");
            text("booking-time").setText("invalid");
            click("booking-review");
            require(node("booking-feedback") != null && label("booking-feedback").getText().contains("HH:mm"),
                    "Validation remains visible after returning from review to edit");
            text("booking-time").setText("14:00");
            click("booking-review");
            ((ScrollPane) node("overview-scroll")).setVvalue(0);
        });
        size(false);
        shot("13-booking-review", false);
        if (polish) { size(true); shot("17-booking-review-compact", true); size(false); }
        check(() -> {
            click("booking-submit");
            require(node("booking-receipt") != null, "Submission shows saved booking receipt");
            require(node("booking-submit") == null, "Receipt prevents accidental duplicate submission");
        });
        shot("09-booking-confirmation", false);
        if (polish) { size(true); shot("18-booking-confirmation-compact", true); size(false); }
        check(() -> {
            click("booking-open-details");
            require(stage.getTitle().equals("AppointEase | Appointment details"), "Window title identifies appointment details");
        });
        shot("08-appointment-details", false);
        if (polish) { size(true); shot("19-appointment-details-compact", true); size(false); }
        check(() -> {
            require(!button("details-confirm").isDisabled(), "Pending appointment can be confirmed");
            click("details-confirm");
            require(node("details-accept") != null, "Lifecycle action asks for explicit confirmation");
            click("details-accept");
            require(button("details-confirm").isDisabled(), "Saved confirmation refreshes available actions");
            require(button("details-complete").isDisabled(), "Future completion is unavailable");
            click("details-reschedule");
            text("reschedule-time").setText("09:00");
            click("reschedule-review");
            click("reschedule-submit");
            require(label("details-feedback").getText().contains("conflicts"), "Conflicting reschedule remains a clear recoverable error");
            click("reschedule-edit");
            text("reschedule-time").setText("14:30");
            click("reschedule-review");
            click("reschedule-submit");
            require(label("details-feedback").getText().contains("updated"), "Reschedule succeeds after correcting the time");
            click("details-cancel");
            click("details-accept");
            require(button("details-reschedule").isDisabled(), "Cancelled appointment cannot be rescheduled");
            click("details-back");
            require(table().getItems().size() == 6, "List refresh includes the new booking after mutations");
            text("appointments-search").setText("seed-ready-001");
            table().getSelectionModel().selectFirst();
            click("appointments-open");
            require(!button("details-complete").isDisabled(), "Elapsed confirmed seed can be completed");
            click("details-complete");
            click("details-accept");
            require(button("details-complete").isDisabled(), "Completion refreshes state");
            click("nav-providers");
        });
        shot("05-providers", false);
        check(() -> {
            click("provider-check");
            require(label("provider-feedback").getText().startsWith("Available"), "Provider exact-time availability works");
            click("provider-book");
            require(combo("booking-provider").getValue() != null, "Provider action preselects the booking provider");
            click("nav-schedule");
        });
        shot("06-schedule", false);
        check(() -> {
            require(stage.getScene().getRoot().lookupAll(".schedule-entry").size() == 4, "Planning view hides cancelled entries by default");
            ((CheckBox) node("schedule-show-cancelled")).setSelected(true);
            require(stage.getScene().getRoot().lookupAll(".schedule-entry").size() == 5, "Show cancelled restores booking history");
        });
        if (polish) {
            check(() -> {
                var first = button("schedule-appointment-desktop-demo-001");
                var second = button("schedule-appointment-desktop-demo-002");
                require(Math.abs(second.getLayoutY() - first.getLayoutY() - 135) < 0.1, "09:00 and 10:30 starts are 90 minutes apart on the grid");
                require(Math.abs(second.getHeight() / first.getHeight() - 2) < 0.01, "60-minute block is twice the height of a 30-minute block");
                combo("schedule-provider").getSelectionModel().selectFirst();
                require(stage.getScene().getRoot().lookupAll(".schedule-entry").size() == 1, "Schedule provider filter retains only the selected provider");
                click("schedule-clear");
                ((CheckBox) node("schedule-show-cancelled")).setSelected(false);
            });
            size(true);
            shot("20-schedule-compact", true);
            check(() -> ((CheckBox) node("schedule-show-cancelled")).setSelected(true));
            check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(1));
            shot("21-schedule-cancelled-compact", true);
            size(false);
            shot("22-schedule-cancelled-desktop", false);
            check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(0));
        }
        check(() -> {
            click("schedule-next");
            require(stage.getScene().getRoot().lookupAll(".schedule-entry").isEmpty(), "Schedule date navigation has an empty state");
            click("schedule-previous");
            ((Button) stage.getScene().getRoot().lookup(".schedule-entry")).fire();
            require(node("appointment-detail-panel") != null, "Timeline entry opens real appointment details");
            click("nav-activity");
        });
        shot("07-activity", false);
        if (polish) { size(true); shot("23-activity-compact", true); size(false); }
        check(() -> {
            require(((ListView<?>) node("activity-records")).getItems().size() >= 20, "Activity refresh shows new lifecycle records");
            combo("activity-channel").setValue("Audit");
            require(((ListView<?>) node("activity-records")).getItems().size() >= 10, "Activity channel filter works");
        });
        // Exercise every view at compact size and verify content fits its viewport.
        size(true);
        for (var page : DesktopController.Page.values()) {
            check(() -> ((DesktopShell) stage.getScene().getRoot()).navigate(page));
            pulse();
            check(() -> {
                var scroll = (ScrollPane) node("overview-scroll");
                require(stage.getTitle().equals("AppointEase | " + DesktopController.title(page)),
                        "Window title follows " + page + " navigation");
                require(scroll.getContent().getLayoutBounds().getWidth() <= scroll.getViewportBounds().getWidth() + 1,
                        "Compact " + page + " fits without horizontal overflow");
            });
        }
        check(() -> {
            button("navigation-toggle").requestFocus();
            stage.toFront();
            new Robot().keyType(KeyCode.TAB);
        });
        pulse();
        check(() -> require(stage.getScene().getFocusOwner() != null && !stage.getScene().getFocusOwner().isDisabled(), "Keyboard Tab reaches an enabled control"));
        if (polish) polishChecks();
        System.out.println((polish ? "S7C" : "S7B") + " DESKTOP CHECK PASSED: " + checks + " assertions, real controls and application services.");
    }

    private void polishChecks() throws Exception {
        size(false);
        check(() -> {
            click("nav-appointments");
            table().getSelectionModel().selectFirst();
        });
        shot("14-appointments-selected-desktop", false);
        size(true);
        shot("15-appointments-selected-compact", true);
        size(false);
        // Create a connected overlap across providers through the actual form, including a quarter-hour start.
        for (var input : java.util.List.of(new String[]{"consult", "09:15"}, new String[]{"health", "09:45"})) {
            check(() -> {
                click("nav-new_appointment");
                combo("booking-customer").getSelectionModel().selectFirst();
                @SuppressWarnings("unchecked") var providers = (ComboBox<ServiceProvider>) node("booking-provider");
                providers.setValue(providers.getItems().stream().filter(p -> p.id().value().contains(input[0])).findFirst().orElseThrow());
                text("booking-time").setText(input[1]);
                click("booking-review");
                click("booking-submit");
                require(node("booking-receipt") != null, "Overlap fixture booked through the form at " + input[1]);
            });
        }
        check(() -> click("nav-schedule"));
        pulse();
        check(() -> {
            var first = button("schedule-appointment-desktop-demo-001");
            var second = scheduleAt("09:15");
            var third = scheduleAt("09:45");
            require(Math.abs(second.getLayoutY() - first.getLayoutY() - 22.5) < 0.1, "Quarter-hour start has its exact proportional offset");
            require(first.getLayoutX() + first.getWidth() <= second.getLayoutX(), "Overlapping providers occupy nonoverlapping columns");
            require(third.getLayoutX() == first.getLayoutX(), "Later overlapping booking reuses the released column");
            require(Math.abs(second.getHeight() / first.getHeight() - 1.5) < 0.01, "45-minute block preserves duration scale");
            require(node("schedule-grid").lookupAll(".grid-time").stream().allMatch(n -> n.getBoundsInLocal().getWidth() > 20),
                    "Time-axis labels have visible dimensions after CSS layout");
            require(node("schedule-grid").lookupAll(".schedule-entry").stream().allMatch(n ->
                    n.getBoundsInParent().getMaxX() <= node("schedule-grid").getLayoutBounds().getWidth() + 1), "All overlap blocks fit the grid width");
        });
        shot("24-schedule-overlap-desktop", false);
        size(true);
        shot("25-schedule-overlap-compact", true);
        check(() -> {
            // Traverse from a visible control into the day, allowing the native ScrollPane to follow focus.
            button("schedule-refresh").requestFocus();
            stage.toFront();
            stage.requestFocus();
        });
        pulse();
        for (int i = 0; i < 6; i++) {
            check(() -> new Robot().keyType(KeyCode.TAB));
            pulse();
        }
        check(() -> {
            var focused = stage.getScene().getFocusOwner();
            require(focused instanceof Button && focused.getStyleClass().contains("schedule-entry"), "Keyboard Tab traverses appointment blocks");
            require(visibleInViewport(focused), "Keyboard focus scrolls the later appointment into view");
            new Robot().keyType(KeyCode.ENTER);
        });
        pulse();
        check(() -> require(node("appointment-detail-panel") != null, "Keyboard Enter opens the focused schedule appointment"));
        size(false);
        check(() -> {
            click("nav-appointments");
            text("appointments-search").setText("seed-ready-001");
            var completed = table().getItems().getFirst();
            click("nav-schedule");
            ((DatePicker) node("schedule-date")).setValue(completed.slot().start().atZone(java.time.ZoneId.of("Europe/Vilnius")).toLocalDate());
            require(stage.getScene().getRoot().lookupAll(".schedule-completed").size() == 1, "Completed history has its distinct schedule treatment");
        });
        shot("26-schedule-completed-desktop", false);
        size(true);
        shot("27-schedule-completed-compact", true);
        for (boolean compact : new boolean[]{false, true}) {
            size(compact);
            for (var page : DesktopController.Page.values()) {
                check(() -> ((DesktopShell) stage.getScene().getRoot()).navigate(page));
                pulse();
                check(() -> {
                    var scroll = (ScrollPane) node("overview-scroll");
                    require(scroll.getContent().getLayoutBounds().getWidth() <= scroll.getViewportBounds().getWidth() + 1,
                            (compact ? "1024x768 " : "1440x900 ") + page + " fits the viewport");
                });
            }
        }
    }

    private Button scheduleAt(String time) {
        return stage.getScene().getRoot().lookupAll(".schedule-entry").stream().map(n -> (Button) n)
                .filter(b -> b.getAccessibleText().startsWith(time)).findFirst().orElseThrow();
    }

    private boolean visibleInViewport(Node node) {
        var viewport = node("overview-scroll").lookup(".viewport");
        var bounds = node.localToScreen(node.getBoundsInLocal());
        var visible = viewport.localToScreen(viewport.getBoundsInLocal());
        return bounds.getMinY() >= visible.getMinY() - 1 && bounds.getMaxY() <= visible.getMaxY() + 1;
    }
    private void size(boolean compact) throws Exception {
        check(() -> { stage.setWidth((compact ? 1024 : 1440) / stage.getOutputScaleX()); stage.setHeight((compact ? 768 : 900) / stage.getOutputScaleY()); });
        pulse();
    }
    private void shot(String name, boolean compact) throws Exception {
        if (!capture) return;
        pulse();
        if (name.equals("10-conflict-error")) {
            // Feedback changes the content height. Scroll only after that layout pulse has settled.
            check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(1));
            pulse();
            check(() -> {
                var message = label("booking-feedback");
                var viewport = node("overview-scroll").lookup(".viewport");
                var bounds = message.localToScreen(message.getBoundsInLocal());
                var visible = viewport.localToScreen(viewport.getBoundsInLocal());
                require(bounds.getMinY() >= visible.getMinY() && bounds.getMaxY() <= visible.getMaxY() + 1,
                        "Conflict feedback is fully visible in the captured viewport");
            });
        }
        var process = new ProcessBuilder("powershell", "-NoProfile", "-File", "scripts/capture-ui.ps1", "-View",
                compact ? "Compact" : "Desktop", "-S7BName", name, "-Slice", polish ? "S7C" : "S7B").inheritIO().start();
        if (!process.waitFor(20, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("Capture timed out: " + name); }
        if (process.exitValue() != 0) throw new AssertionError("Capture failed: " + name);
        pulse();
    }
    private void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); checks++; System.out.println("PASS: " + message); }
    private Node node(String id) { return stage.getScene().lookup("#" + id); }
    private Button button(String id) { return (Button) node(id); }
    private TextField text(String id) { return (TextField) node(id); }
    private Label label(String id) { return (Label) node(id); }
    @SuppressWarnings("unchecked") private ComboBox<Object> combo(String id) { return (ComboBox<Object>) node(id); }
    @SuppressWarnings("unchecked") private TableView<Appointment> table() { return (TableView<Appointment>) node("appointments-table"); }
    private void click(String id) { var b = button(id); if (b == null || b.isDisabled()) throw new AssertionError("Unavailable control: " + id); b.fire(); }
    private void check(Runnable action) throws Exception { fx(() -> { action.run(); stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); return null; }); }
    private static void pulse() throws Exception {
        var ready = new CompletableFuture<Void>();
        Platform.runLater(() -> { var pause = new PauseTransition(Duration.millis(250)); pause.setOnFinished(e -> ready.complete(null)); pause.play(); });
        ready.get(10, TimeUnit.SECONDS);
    }
    private static <T> T fx(Callable<T> action) throws Exception {
        var result = new CompletableFuture<T>();
        Platform.runLater(() -> { try { result.complete(action.call()); } catch (Throwable ex) { result.completeExceptionally(ex); } });
        return result.get(15, TimeUnit.SECONDS);
    }
}
