package com.appointease.presentation.desktop;

import com.appointease.Main;
import com.appointease.ApplicationContext;
import com.appointease.infrastructure.seed.DesktopDemoSeed;
import com.appointease.domain.model.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Opt-in real Windows scene/resize/Robot checks against the packaged presentation and facade. */
public final class DesktopS8CCheck {
    private final Stage stage;
    private final DesktopController controller;
    private final LocalDate seedDay;
    private final boolean capture;
    private int checks;
    private LocalDate busyDay;
    private DesktopS8CCheck(Stage stage, DesktopController controller, LocalDate seedDay, boolean capture) {
        this.stage = stage; this.controller = controller; this.seedDay = seedDay; this.capture = capture;
    }
    public static void main(String[] args) throws Exception {
        Platform.startup(() -> { }); Platform.setImplicitExit(false);
        var check = fx(() -> {
            ApplicationContext context = Main.createContext(Clock.systemUTC(), ZoneId.of(System.getProperty("appointease.zone", "Europe/Vilnius")));
            var day = DesktopDemoSeed.populate(context.facade(), context.time());
            var controller = new DesktopController(context.facade(), context.customers(), context.time(), context.notifications(), context.audit());
            var shell = new DesktopShell(controller, new OverviewView(new OverviewModel(context.facade(), context.customers(), context.time()), day), day);
            var stage = new Stage(); var scene = new Scene(shell);
            scene.getStylesheets().add(Objects.requireNonNull(DesktopS8CCheck.class.getResource("/com/appointease/presentation/desktop/theme.css")).toExternalForm());
            stage.setScene(scene); stage.setMinWidth(800); stage.setMinHeight(580);
            stage.titleProperty().bind(shell.pageTitleProperty().map(s -> "AppointEase | " + s));
            stage.show();
            return new DesktopS8CCheck(stage, controller, day, List.of(args).contains("--capture"));
        });
        try { check.run(); } finally { fx(() -> { check.stage.close(); return null; }); Platform.exit(); }
    }
    private void run() throws Exception {
        size(false);
        check(() -> { click("nav-schedule"); date().setValue(seedDay.plusWeeks(1)); click("schedule-view-month");
            combo("schedule-provider").setValue(controller.providers().getFirst());
            ((CheckBox) node("schedule-show-cancelled")).setSelected(true); });
        for (String mode : List.of("day", "week", "month")) {
            check(() -> click("schedule-view-" + mode)); pulse();
            check(() -> { outer().setVvalue(0.3); click("nav-activity"); click("nav-schedule"); }); pulse();
            check(() -> {
                require(((ToggleButton) node("schedule-view-" + mode)).isSelected(), "Re-entry preserves " + mode);
                require(date().getValue().equals(seedDay.plusWeeks(1)), "Re-entry preserves selected date");
                require(combo("schedule-provider").getValue().equals(controller.providers().getFirst()), "Re-entry preserves provider");
                require(((CheckBox) node("schedule-show-cancelled")).isSelected(), "Re-entry preserves cancelled setting");
            });
        }
        size(true);
        check(() -> { date().setValue(seedDay); click("schedule-clear"); click("schedule-view-week"); }); pulse();
        check(() -> {
            require(node("schedule-week-affordance").isVisible(), "Compact Week exposes named horizontal controls");
            require(node("schedule-week-left").isDisabled(), "Earlier days disabled at left edge");
            click("schedule-week-right"); require(week().getHvalue() > 0, "Later days moves the viewport");
            week().setHvalue(0.55); outer().setVvalue(0.35);
        });
        pulse(); shot("03-schedule-before-navigation", true);
        double savedVertical = fx(() -> outer().getVvalue());
        double savedHorizontal = fx(() -> week().getHvalue());
        check(() -> { click("nav-services"); click("nav-schedule"); }); pulse();
        check(() -> {
            require(Math.abs(outer().getVvalue() - savedVertical) < 0.01, "Navigation restores vertical scroll");
            require(Math.abs(week().getHvalue() - savedHorizontal) < 0.01, "Navigation restores horizontal scroll");
        });
        shot("04-schedule-after-navigation", true);
        check(() -> { outer().setVvalue(0); week().setHvalue(0); });
        shot("05-week-compact-scroll-controls", true);
        nativeClick("schedule-week-right");
        check(() -> week().setHvalue(0)); pulse(); key(KeyCode.ENTER);
        check(() -> require(week().getHvalue() > 0, "Keyboard activates horizontal-scroll control"));
        double weekPosition = fx(() -> week().getHvalue());
        check(() -> click("schedule-appointment-desktop-demo-001"));
        check(() -> click("details-back")); pulse();
        check(() -> require(((ToggleButton) node("schedule-view-week")).isSelected()
                && Math.abs(week().getHvalue() - weekPosition) < 0.01, "Week details restores mode and horizontal position"));
        check(() -> { click("schedule-view-day"); date().setValue(seedDay);
            node("schedule-appointment-desktop-demo-003").requestFocus(); outer().setVvalue(0.4); }); pulse();
        double detailScroll = fx(() -> outer().getVvalue());
        key(KeyCode.ENTER);
        check(() -> require(((Button) node("details-back")).getText().equals("Back to Schedule"), "Calendar details names its return destination"));
        size(false); shot("07-details-back-to-schedule", false);
        check(() -> { click("details-confirm"); click("details-accept"); click("details-back"); }); pulse();
        check(() -> {
            require(((ToggleButton) node("schedule-view-day")).isSelected() && date().getValue().equals(seedDay), "Details returns to original calendar context");
            require(node("schedule-appointment-desktop-demo-003").getStyleClass().contains("schedule-confirmed"), "Return refreshes confirmed appointment");
            require(stage.getScene().getFocusOwner() == node("schedule-appointment-desktop-demo-003"), "Return restores originating appointment focus");
            require(Math.abs(outer().getVvalue() - detailScroll) < 0.01, "Details return restores vertical position");
        });
        shot("08-schedule-after-details-mutation", false);
        check(() -> {
            controller.reschedule(controller.prepareMove(new AppointmentId("desktop-demo-003"), seedDay.plusWeeks(1), "13:00", false));
            click("schedule-refresh");
            require(node("schedule-appointment-desktop-demo-003") == null, "Explicit refresh removes moved appointment from old day");
            click("schedule-view-week");
            date().setValue(seedDay.plusWeeks(1));
        }); pulse();
        check(() -> {
            require(node("schedule-appointment-desktop-demo-003") != null, "Week refresh includes moved appointment at new date");
            controller.act(DesktopController.Action.CANCEL, "s8c-cancel", new AppointmentId("desktop-demo-003"));
            ((CheckBox) node("schedule-show-cancelled")).setSelected(false);
            click("nav-appointments"); click("nav-schedule");
        }); pulse();
        check(() -> require(node("schedule-appointment-desktop-demo-003") == null, "Re-entry hides newly cancelled appointment"));
        monthKeyboard();
        activity();
        activityRecovery();
        performance();
        System.out.println("S8C verification passed: " + checks + " assertions.");
    }
    private ScrollPane week() { return (ScrollPane) node("schedule-week-scroll"); }
    private void key(KeyCode key) throws Exception {
        check(() -> require(stage.isFocused(), "Native keyboard has foreground Windows focus"));
        check(() -> new Robot().keyPress(key)); pulse();
        check(() -> new Robot().keyRelease(key)); pulse();
    }
    private void monthKeyboard() throws Exception {
        size(true);
        check(() -> { click("schedule-view-month"); date().setValue(LocalDate.of(2028, 1, 31));
            node("schedule-month-day-2028-01-31").requestFocus(); outer().setVvalue(1); stage.toFront(); stage.requestFocus(); }); pulse();
        key(KeyCode.RIGHT);
        check(() -> require(date().getValue().equals(LocalDate.of(2028, 2, 1)) && stage.getScene().getFocusOwner() == node("schedule-month-day-2028-02-01"), "Native Right traverses month boundary with focus"));
        key(KeyCode.DOWN); key(KeyCode.HOME);
        check(() -> require(date().getValue().equals(LocalDate.of(2028, 2, 7)), "Native Down and Home move by week then Monday"));
        key(KeyCode.END); key(KeyCode.PAGE_DOWN);
        check(() -> require(date().getValue().equals(LocalDate.of(2028, 3, 13)), "Native End and Page Down preserve date semantics"));
        key(KeyCode.PAGE_UP); key(KeyCode.UP); key(KeyCode.LEFT);
        check(() -> {
            require(date().getValue().equals(LocalDate.of(2028, 2, 5)), "Native Page Up, Up and Left traverse dates");
            var focus = stage.getScene().getFocusOwner();
            require(focus instanceof Button && ((Button) focus).getBorder().getStrokes().getFirst().getWidths().getTop() == 2,
                    "Focused Month cell has visible two-pixel border");
            require(node("schedule-month-grid").lookupAll(".calendar-month-cell").stream().filter(Node::isFocusTraversable).count() == 1,
                    "Month grid has one Tab entry point");
        });
        shot("06-month-keyboard-focus", true);
        key(KeyCode.SPACE);
        check(() -> require(((ToggleButton) node("schedule-view-day")).isSelected() && date().getValue().equals(LocalDate.of(2028, 2, 5)), "Space opens keyboard-selected Day"));
        check(() -> { click("schedule-view-month"); node("schedule-month-day-2028-02-05").requestFocus(); }); pulse(); key(KeyCode.ENTER);
        check(() -> require(((ToggleButton) node("schedule-view-day")).isSelected(), "Enter opens Month date"));
    }
    @SuppressWarnings("unchecked") private ListView<DesktopController.Activity> feed() { return (ListView<DesktopController.Activity>) node("activity-records"); }
    private void activity() throws Exception {
        size(false); check(() -> click("nav-activity")); pulse();
        check(() -> {
            require(feed().getItems().size() == controller.activity().size(), "Activity includes every notification and audit record");
            require(feed().getItems().getFirst().event().sequence() >= feed().getItems().getLast().event().sequence(), "Activity newest first");
            require(node("activity-details").getBoundsInParent().getMinX() > feed().getBoundsInParent().getMaxX(), "Desktop details sit beside the feed");
            double ratio = feed().getWidth() / (feed().getWidth() + node("activity-details").getLayoutBounds().getWidth());
            require(ratio >= 0.60 && ratio <= 0.65, "Desktop feed uses 60-65 percent of columns");
            feed().getSelectionModel().select(3); feed().requestFocus(); stage.toFront(); stage.requestFocus();
        }); pulse(); key(KeyCode.DOWN);
        check(() -> {
            require(feed().getSelectionModel().getSelectedIndex() == 4, "Native Down selects next Activity event");
            var selected = feed().getSelectionModel().getSelectedItem();
            require(node("activity-details").getAccessibleText().contains(selected.event().id()), "Details match selected event");
            click("activity-refresh"); require(feed().getSelectionModel().getSelectedItem().equals(selected), "Refresh retains selected event and channel");
            combo("activity-channel").setValue("Audit");
            require(feed().getItems().stream().allMatch(a -> a.channel().equals("Audit")), "Audit filter excludes notifications");
            combo("activity-channel").setValue("Notification (simulated)");
            require(feed().getItems().stream().allMatch(a -> a.channel().equals("Notification (simulated)")), "Notification filter excludes audit");
            combo("activity-channel").setValue("All records");
        }); pulse(); shot("01-activity-desktop-selected", false);
        check(() -> {
            require(node("activity-details").lookupAll(".row-secondary").stream().map(n -> (Label) n)
                    .allMatch(l -> l.getHeight() + 1 >= l.prefHeight(l.getWidth())), "Selected event fields retain full wrapped height");
        });
        var selected = fx(() -> feed().getSelectionModel().getSelectedItem());
        size(true);
        check(() -> {
            require(node("activity-details").getBoundsInParent().getMinY() >= feed().getBoundsInParent().getMaxY(), "Compact Activity stacks details below feed");
            require(feed().getSelectionModel().getSelectedItem().equals(selected), "Compact resize retains selection");
            require(outer().getContent().getLayoutBounds().getWidth() <= outer().getViewportBounds().getWidth() + 1, "Compact Activity fits horizontally");
            outer().setVvalue(0.5);
        }); pulse(); shot("02-activity-compact", true);
        check(() -> outer().setVvalue(1)); pulse();
        shot("11-activity-compact-details", true);
    }
    private void activityRecovery() throws Exception {
        check(() -> {
            var context = Main.createContext(Clock.systemUTC(), controller.time().zone());
            var failed = new java.util.concurrent.atomic.AtomicBoolean(false);
            var data = new ArrayList<>(List.of(controller.activity().getFirst().event()));
            var empty = new DesktopController(context.facade(), context.customers(), context.time(), List::of,
                    () -> { if (failed.get()) throw new IllegalStateException("Test history unavailable"); return List.copyOf(data); });
            outer().setContent(new ActivityView(empty));
            require(feed().getSelectionModel().getSelectedItem() != null, "Single-event Activity selects its event");
            data.clear(); click("activity-refresh");
            require(feed().getItems().isEmpty() && node("activity-details").lookupAll(".event-type").isEmpty()
                    && node("activity-details").getAccessibleText() == null, "Empty Activity has no stale selection or accessible details");
            data.add(controller.activity().getFirst().event()); click("activity-refresh");
            failed.set(true); click("activity-refresh");
            require(((Label) node("activity-feedback")).getText().contains("Test history unavailable") && feed().getItems().isEmpty()
                    && node("activity-details").getAccessibleText() == null, "Activity reports supplier failure without stale events");
            failed.set(false); data.clear(); click("activity-refresh");
            require(((Label) node("activity-feedback")).getText().startsWith("0 records")
                    && node("activity-feedback").getAccessibleText() == null, "Activity refresh recovers without stale accessible error");
            click("nav-activity");
        }); pulse();
    }
    private void performance() throws Exception {
        size(false);
        check(() -> {
            busyDay = CalendarModel.range(seedDay, CalendarModel.View.WEEK).start().plusWeeks(2);
            long start = System.nanoTime();
            for (int week = 0; week < 5; week++) for (int d = 0; d < 5; d++) for (var provider : controller.providers()) {
                for (String time : List.of("09:00", "10:30", "12:00", "13:30", "15:00")) {
                    var receipt = controller.book(controller.prepare(new DesktopController.Draft(controller.customers().getFirst(), provider,
                            controller.services(provider).getFirst(), busyDay.plusWeeks(week).plusDays(d), time,
                            provider.modes().contains(AppointmentMode.ONLINE) ? AppointmentMode.ONLINE : AppointmentMode.IN_PERSON, false, false, false)));
                    controller.act(DesktopController.Action.CONFIRM, UUID.randomUUID().toString(), receipt.appointment().id());
                }
            }
            System.out.printf(Locale.ROOT, "PERF fixture: %.2f ms%n", (System.nanoTime() - start) / 1e6);
            require(controller.appointments("", null, null, null).size() == 505, "Larger fixture has 505 real appointments");
            System.out.println("PERF activity records: " + controller.activity().size());
        });
        measure("appointments query 505", () -> controller.appointments("", null, null, null));
        measure("activity query", controller::activity);
        measure("Activity refresh + CSS/layout", () -> { click("activity-refresh"); stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); });
        check(() -> {
            require(feed().lookupAll(".list-cell").size() < 40, "Large Activity history creates bounded visible cells");
            feed().getSelectionModel().selectLast(); feed().scrollTo(feed().getItems().size() - 1);
        }); pulse();
        check(() -> require(node("activity-details").getAccessibleText().contains(feed().getItems().getLast().event().id()), "Oldest large-history event remains selectable"));
        shot("09-activity-large-history", false);
        check(() -> { click("nav-schedule"); date().setValue(busyDay); click("schedule-view-week"); click("schedule-clear"); }); pulse();
        measure("Week refresh + CSS/layout, 100 appointments", () -> { click("schedule-refresh"); stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); });
        check(() -> require(entries() == 100, "Busy Week renders every appointment"));
        size(true); shot("10-week-large-compact", true);
        check(() -> { require(node("schedule-week-affordance").isVisible(), "Large compact week keeps horizontal controls"); click("schedule-view-month"); }); pulse();
        measure("Month refresh + CSS/layout", () -> { click("schedule-refresh"); stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); });
    }
    private void measure(String name, Runnable work) throws Exception {
        for (int i = 0; i < 3; i++) check(work);
        var samples = new double[12];
        for (int i = 0; i < samples.length; i++) { final int index = i; check(() -> { long start = System.nanoTime(); work.run(); samples[index] = (System.nanoTime() - start) / 1e6; }); }
        Arrays.sort(samples);
        System.out.printf(Locale.ROOT, "PERF %s: median %.2f ms; max %.2f ms (12 samples after 3 warmups)%n", name, (samples[5] + samples[6]) / 2, samples[11]);
    }
    private long entries() {
        stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout();
        return stage.getScene().getRoot().lookupAll(".schedule-entry").size();
    }
    private ScrollPane outer() { return (ScrollPane) node("overview-scroll"); }
    private DatePicker date() { return (DatePicker) node("schedule-date"); }
    private Node node(String id) { return stage.getScene().lookup("#" + id); }
    @SuppressWarnings("unchecked") private ComboBox<Object> combo(String id) { return (ComboBox<Object>) node(id); }
    private void click(String id) { var b = (ButtonBase) node(id); if (b == null || b.isDisabled()) throw new AssertionError("Unavailable control: " + id); b.fire(); }
    private void nativeClick(String id) throws Exception {
        check(() -> { stage.toFront(); stage.requestFocus(); }); pulse();
        check(() -> { var bounds = node(id).localToScreen(node(id).getBoundsInLocal()); var robot = new Robot();
            robot.mouseMove(bounds.getCenterX(), bounds.getCenterY()); }); pulse();
        check(() -> new Robot().mousePress(MouseButton.PRIMARY)); pulse();
        check(() -> new Robot().mouseRelease(MouseButton.PRIMARY)); pulse();
    }
    private void size(boolean compact) throws Exception {
        check(() -> { stage.setWidth((compact ? 1024 : 1440) / stage.getOutputScaleX()); stage.setHeight((compact ? 768 : 900) / stage.getOutputScaleY());
            stage.setX(20 / stage.getOutputScaleX()); stage.setY(20 / stage.getOutputScaleY()); stage.setAlwaysOnTop(true); stage.toFront(); }); pulse();
    }
    private void shot(String name, boolean compact) throws Exception {
        if (!capture) return; pulse();
        // Preserve the tested scroll and focus in the native capture.
        check(() -> new Robot().mouseMove(stage.getX() + stage.getWidth() / 2, stage.getY() + 14)); pulse();
        var p = new ProcessBuilder("powershell", "-NoProfile", "-File", "scripts/capture-ui.ps1", "-View", compact ? "Compact" : "Desktop",
                "-S7BName", name, "-Slice", "S8C").inheritIO().start();
        if (!p.waitFor(20, TimeUnit.SECONDS)) { p.destroyForcibly(); throw new AssertionError("Capture timed out"); }
        if (p.exitValue() != 0) throw new AssertionError("Capture failed: " + name); pulse();
    }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; System.out.println("PASS: " + message); }
    private void check(Runnable action) throws Exception { fx(() -> { action.run(); stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); return null; }); }
    private static void pulse() throws Exception {
        var result = new CompletableFuture<Void>(); Platform.runLater(() -> { var pause = new PauseTransition(Duration.millis(250)); pause.setOnFinished(e -> result.complete(null)); pause.play(); }); result.get(10, TimeUnit.SECONDS);
    }
    private static <T> T fx(Callable<T> action) throws Exception {
        var result = new CompletableFuture<T>(); Platform.runLater(() -> { try { result.complete(action.call()); } catch (Throwable ex) { result.completeExceptionally(ex); } }); return result.get(20, TimeUnit.SECONDS);
    }
}
