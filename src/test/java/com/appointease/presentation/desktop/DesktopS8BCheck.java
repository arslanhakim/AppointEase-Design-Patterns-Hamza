package com.appointease.presentation.desktop;

import com.appointease.Main;
import com.appointease.ApplicationContext;
import com.appointease.infrastructure.seed.DesktopDemoSeed;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Orientation;
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
public final class DesktopS8BCheck {
    private final Stage stage;
    private final DesktopController controller;
    private final LocalDate seedDay;
    private final boolean capture;
    private int checks;
    private double desktopTableHeight;
    private Appointment selected;
    private LocalDate busyDay;
    private DesktopS8BCheck(Stage stage, DesktopController controller, LocalDate seedDay, boolean capture) {
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
            scene.getStylesheets().add(Objects.requireNonNull(DesktopS8BCheck.class.getResource("/com/appointease/presentation/desktop/theme.css")).toExternalForm());
            stage.setScene(scene); stage.setMinWidth(800); stage.setMinHeight(580);
            stage.titleProperty().bind(shell.pageTitleProperty().map(s -> "AppointEase | " + s));
            stage.show();
            return new DesktopS8BCheck(stage, controller, day, List.of(args).contains("--capture"));
        });
        try { check.run(); } finally { fx(() -> { check.stage.close(); return null; }); Platform.exit(); }
    }
    private void run() throws Exception {
        size(false); check(() -> click("nav-appointments")); pulse();
        check(() -> {
            require(table().getItems().size() == 5, "Small seed dataset remains intact");
            table().getSelectionModel().selectFirst(); selected = table().getSelectionModel().getSelectedItem();
            desktopTableHeight = table().getHeight();
            System.out.println("Desktop table height: " + desktopTableHeight + "; viewport: " + outer().getViewportBounds());
            require(desktopTableHeight > 150 && Math.abs(desktopTableHeight - 320) > 1, "Appointments table uses available height instead of fixed 320 pixels");
            tableFits();
        });
        shot("01-appointments-full-height-desktop", false);
        size(true);
        check(() -> {
            require(table().getHeight() < desktopTableHeight - 40, "Actual compact resize shrinks table");
            require(table().getSelectionModel().getSelectedItem().id().equals(selected.id()), "Resize preserves selected appointment");
            tableFits();
            text("appointments-search").setText(selected.id().value());
            combo("appointments-provider").setValue(selected.provider());
            combo("appointments-status").setValue(selected.status());
            require(table().getItems().size() == 1, "Combined filters select one real record");
        });
        pulse(); check(() -> require(!verticalVisible(table()), "One-row dataset has no internal vertical scrollbar"));
        shot("02-appointments-compact", true);
        for (boolean compact : List.of(false, true, false, true, false)) {
            size(compact);
            check(() -> {
                require(text("appointments-search").getText().equals(selected.id().value())
                        && combo("appointments-provider").getValue().equals(selected.provider())
                        && combo("appointments-status").getValue().equals(selected.status()), "Resize retains all three filter values");
                require(table().getSelectionModel().getSelectedItem().id().equals(selected.id()), "Repeated resize retains selection");
                tableFits();
            });
        }
        check(() -> {
            click("appointments-refresh");
            require(table().getSelectionModel().getSelectedItem().id().equals(selected.id()), "Explicit refresh preserves selected record");
            text("appointments-search").setText("no-record-matches-this-search");
            require(table().getItems().isEmpty(), "Empty table retains its filters and placeholder");
            click("appointments-clear");
            busyDay = CalendarModel.range(seedDay, CalendarModel.View.WEEK).start().plusWeeks(1);
            for (int d = 0; d < 5; d++) for (String code : List.of("health", "tutor")) {
                var provider = controller.providers().stream().filter(p -> p.id().value().contains(code)).findFirst().orElseThrow();
                for (String time : List.of("09:00", "10:30", "12:00", "13:30", "15:00"))
                    controller.book(controller.prepare(new DesktopController.Draft(controller.customers().getFirst(), provider,
                            controller.services(provider).getFirst(), busyDay.plusDays(d), time, AppointmentMode.ONLINE, false, false, false)));
            }
            var original = controller.appointments("", null, new ProviderId("provider-health-001"), busyDay).getFirst();
            controller.act(DesktopController.Action.CANCEL, "s8b-cancel", original.id());
            controller.book(controller.prepare(new DesktopController.Draft(original.customer(), original.provider(), original.service(),
                    busyDay, "09:00", AppointmentMode.ONLINE, false, false, false)));
            click("appointments-refresh");
            require(table().getItems().size() == 56, "Large fixture has 56 facade-created records including cancelled history");
            table().getSelectionModel().selectLast(); selected = table().getSelectionModel().getSelectedItem();
            table().scrollTo(selected);
        });
        pulse();
        check(() -> { require(verticalVisible(table()), "Large dataset scrolls inside the table"); tableFits(); });
        shot("03-appointments-large-dataset", false);
        size(true); check(() -> { tableFits(); require(table().getSelectionModel().getSelectedItem().equals(selected), "Large-table selection survives compact resize"); });
        shot("10-appointments-large-compact", true);
        size(false);
        check(() -> { click("nav-schedule"); require(date().getValue().equals(seedDay), "Initial schedule selected date is retained"); });
        shot("04-day-schedule", false);
        nativeClick("schedule-view-week");
        check(() -> {
            require(node("schedule-week-grid").lookupAll(".calendar-week-column").size() == 7, "Week renders seven distinct columns");
            require(date().getValue().equals(seedDay), "Switch to Week preserves selected date");
            require(node("schedule-week-day-" + seedDay).getStyleClass().contains("calendar-selected-day"), "Week marks selected day");
            var first = (Button) node("schedule-appointment-desktop-demo-001");
            var second = (Button) node("schedule-appointment-desktop-demo-002");
            require(Math.abs(second.getLayoutY() - first.getLayoutY() - 135) < 0.1
                    && Math.abs(first.getHeight() - 45) < 0.1 && Math.abs(second.getHeight() - 90) < 0.1,
                    "Week keeps proportional 30/60-minute durations and exact 90-minute start separation");
            require(entries() == controller.appointments("", null, null, null).stream().filter(a ->
                    CalendarModel.range(seedDay, CalendarModel.View.WEEK).days().contains(a.slot().start().atZone(controller.time().zone()).toLocalDate())).count(), "Week shows every matching record");
        });
        shot("05-week-desktop", false);
        size(true); shot("06-week-compact", true);
        check(() -> {
            var scroll = (ScrollPane) node("schedule-week-scroll");
            require(scroll.getContent().getLayoutBounds().getWidth() > scroll.getViewportBounds().getWidth(), "Compact week provides horizontal overflow");
            require(scroll.getVbarPolicy() == ScrollPane.ScrollBarPolicy.NEVER, "Week has only one vertical scroll owner");
            scroll.setHvalue(1);
        });
        pulse();
        check(() -> {
            var lastDay = CalendarModel.range(seedDay, CalendarModel.View.WEEK).days().getLast();
            var scroll = (ScrollPane) node("schedule-week-scroll");
            var last = node("schedule-week-day-" + lastDay).localToScene(node("schedule-week-day-" + lastDay).getBoundsInLocal());
            var viewport = scroll.lookup(".viewport").localToScene(scroll.lookup(".viewport").getBoundsInLocal());
            require(last.getMaxX() <= viewport.getMaxX() + 1 && last.getMinX() >= viewport.getMinX(), "Horizontal scroll reaches Sunday");
            click("schedule-next"); require(date().getValue().equals(seedDay.plusWeeks(1)), "Next navigates one week");
            click("schedule-previous"); require(date().getValue().equals(seedDay), "Previous restores week date");
            date().setValue(busyDay.plusWeeks(2));
            require(entries() == 0 && node("schedule-week-grid").lookupAll(".calendar-week-column").size() == 7,
                    "Empty week retains seven days and its time grid");
            date().setValue(busyDay); ((CheckBox) node("schedule-show-cancelled")).setSelected(true);
        });
        size(false);
        check(() -> {
            require(entries() == 51, "Busy week includes all active and cancelled records");
            var blocks = node("schedule-week-grid-" + busyDay).lookupAll(".schedule-entry").stream().toList();
            require(blocks.size() == 11, "Overlapping day keeps all eleven records");
            require(blocks.stream().allMatch(a -> blocks.stream().allMatch(b -> a == b
                    || !a.getBoundsInParent().intersects(b.getBoundsInParent()))), "All week blocks retain unobscured click targets");
            require(blocks.stream().allMatch(n -> n.getBoundsInLocal().getWidth() >= 50), "Dense week keeps practical click widths");
        });
        shot("11-week-overlaps", false);
        check(() -> {
            combo("schedule-provider").getSelectionModel().select(controller.providers().stream().filter(p -> p.id().value().contains("health")).findFirst().orElseThrow());
            require(entries() == 26, "Week provider filter includes cancelled record for same provider");
            ((CheckBox) node("schedule-show-cancelled")).setSelected(false);
            require(entries() == 25, "Week consistently hides cancelled record");
            click("schedule-view-month");
            require(date().getValue().equals(busyDay), "Month switch retains selected date");
            require(combo("schedule-provider").getValue() != null, "Month switch retains provider");
            var cell = (Button) node("schedule-month-day-" + busyDay);
            require(cell.getAccessibleText().contains("5 non-cancelled, 0 cancelled"), "Month counts respect provider and cancelled visibility");
            require(cell.getStyleClass().contains("calendar-selected-day"), "Month highlights selected date");
            ((CheckBox) node("schedule-show-cancelled")).setSelected(true);
            require(((Button) node("schedule-month-day-" + busyDay)).getAccessibleText().contains("1 cancelled"), "Month distinguishes cancelled history");
            click("schedule-clear");
        });
        shot("07-month-desktop", false);
        size(true); shot("08-month-compact", true);
        check(() -> {
            require(node("schedule-month-grid").getLayoutBounds().getWidth() <= outer().getViewportBounds().getWidth(), "Compact month fits horizontally");
            require(node("schedule-month-grid").lookupAll(".calendar-month-cell").size() == CalendarModel.range(busyDay, CalendarModel.View.MONTH).days().size(), "Month includes every leading and trailing day");
            click("schedule-next"); require(date().getValue().equals(busyDay.plusMonths(1)), "Next month navigation");
            click("schedule-previous"); require(date().getValue().equals(busyDay), "Previous month navigation");
            combo("schedule-provider").setValue(controller.providers().stream().filter(p -> p.id().value().contains("health")).findFirst().orElseThrow());
        });
        nativeClick("schedule-month-day-" + busyDay.plusDays(1));
        check(() -> {
            require(((ToggleButton) node("schedule-view-day")).isSelected() && date().getValue().equals(busyDay.plusDays(1)), "Native date click changes date and opens that day");
            require(combo("schedule-provider").getValue() != null && ((CheckBox) node("schedule-show-cancelled")).isSelected(), "Date selection retains provider and cancelled setting");
            require(entries() == 5, "Selected date shows the matching five bookings");
        });
        size(false); shot("12-selected-day", false);
        check(() -> {
            var button = (Button) stage.getScene().getRoot().lookup(".schedule-entry");
            button.requestFocus(); stage.toFront(); stage.requestFocus();
        });
        pulse(); check(() -> new Robot().keyPress(KeyCode.ENTER)); pulse();
        check(() -> new Robot().keyRelease(KeyCode.ENTER)); pulse();
        check(() -> require(node("appointment-detail-panel") != null, "Keyboard Enter opens selected calendar appointment details"));
        shot("09-selected-appointment-details", false);
        check(() -> { click("nav-schedule"); click("schedule-view-week"); });
        pulse();
        check(() -> { ((Button) stage.getScene().getRoot().lookup(".schedule-entry")).fire(); require(node("appointment-detail-panel") != null, "Week appointment opens real details"); });
        System.out.println("S8B verification passed: " + checks + " assertions.");
    }
    private void tableFits() {
        var viewport = outer().lookup(".viewport").localToScene(outer().lookup(".viewport").getBoundsInLocal());
        var tableBounds = table().localToScene(table().getBoundsInLocal());
        require(table().getHeight() >= 150 && Math.abs(viewport.getMaxY() - tableBounds.getMaxY() - (stage.getWidth() < 1000 ? 24 : 30)) <= 3,
                "Table fills available height with practical minimum and page padding");
        require(outer().getContent().getLayoutBounds().getHeight() <= outer().getViewportBounds().getHeight() + 1,
                "Appointments has no nested outer vertical overflow");
        require(tableBounds.getMaxX() <= viewport.getMaxX() + 1, "Table columns fit window width");
    }
    private boolean verticalVisible(Node parent) {
        return parent.lookupAll(".scroll-bar").stream().filter(n -> n instanceof ScrollBar)
                .map(n -> (ScrollBar) n).anyMatch(s -> s.getOrientation() == Orientation.VERTICAL && s.isVisible()
                        && (!(parent instanceof ScrollPane) || s.getParent().getParent() == parent));
    }
    private long entries() {
        stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout();
        return stage.getScene().getRoot().lookupAll(".schedule-entry").size();
    }
    private ScrollPane outer() { return (ScrollPane) node("overview-scroll"); }
    private DatePicker date() { return (DatePicker) node("schedule-date"); }
    private Node node(String id) { return stage.getScene().lookup("#" + id); }
    private TextField text(String id) { return (TextField) node(id); }
    @SuppressWarnings("unchecked") private ComboBox<Object> combo(String id) { return (ComboBox<Object>) node(id); }
    @SuppressWarnings("unchecked") private TableView<Appointment> table() { return (TableView<Appointment>) node("appointments-table"); }
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
        check(() -> { outer().setVvalue(0); stage.getScene().getRoot().requestFocus(); }); pulse();
        var p = new ProcessBuilder("powershell", "-NoProfile", "-File", "scripts/capture-ui.ps1", "-View", compact ? "Compact" : "Desktop",
                "-S7BName", name, "-Slice", "S8B").inheritIO().start();
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
