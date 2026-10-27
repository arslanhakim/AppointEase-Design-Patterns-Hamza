package com.appointease.presentation.desktop;

import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/** Elapsed-time day grid. Overlapping intervals share columns, including cancelled history. */
final class ScheduleView extends VBox {
    private final DesktopController controller;
    private final Consumer<AppointmentId> details;
    private final DatePicker date;
    private final ComboBox<ServiceProvider> provider;
    private final CheckBox cancelled = new CheckBox("Show cancelled");
    private final VBox timeline = new VBox(8);
    private final Label title = Ui.text("", "section-title");
    private final Label count = Ui.text("", "muted");
    private final Label heading = Ui.label("Schedule", "page-title");
    private final Label advice = Ui.text("Open an appointment or date for details. Empty space does not guarantee availability.", "muted");
    private final FlowPane legend = Ui.actions(Ui.status(AppointmentStatus.PENDING), Ui.status(AppointmentStatus.CONFIRMED),
            Ui.status(AppointmentStatus.COMPLETED), Ui.status(AppointmentStatus.CANCELLED), count);
    private final ToggleGroup views = new ToggleGroup();
    private CalendarModel.View view = CalendarModel.View.DAY;
    private double weekScroll;
    private boolean reloading;
    private String focusId;

    ScheduleView(DesktopController controller, LocalDate initialDay, Consumer<AppointmentId> details, Runnable list) {
        this.controller = controller;
        this.details = details;
        getStyleClass().add("page");
        setSpacing(10);
        setMinWidth(0);
        date = Ui.date("schedule-date", initialDay);
        provider = Ui.combo("schedule-provider", controller.providers(), ServiceProvider::name);
        provider.setPromptText("All providers");
        cancelled.setId("schedule-show-cancelled");
        timeline.setId("schedule-timeline");
        timeline.setMinWidth(0);
        var selector = new HBox(2);
        for (var mode : CalendarModel.View.values()) {
            var button = new ToggleButton(DesktopController.title(mode));
            button.setId("schedule-view-" + mode.name().toLowerCase(Locale.ROOT));
            button.setUserData(mode);
            button.setToggleGroup(views);
            button.getStyleClass().add("calendar-view-toggle");
            button.setAccessibleText(DesktopController.title(mode) + " schedule");
            button.setSelected(mode == view);
            selector.getChildren().add(button);
        }
        views.selectedToggleProperty().addListener((o, old, selected) -> {
            if (selected == null) { views.selectToggle(old); return; }
            view = (CalendarModel.View) selected.getUserData(); refresh();
        });
        date.valueProperty().addListener((o, a, b) -> refresh());
        provider.valueProperty().addListener((o, a, b) -> refresh());
        cancelled.selectedProperty().addListener((o, a, b) -> refresh());
        getChildren().setAll(heading,
                Ui.text("" + controller.time().zone() + " · Appointment times and durations", "subtitle"),
                Ui.actions(selector, Ui.action("Previous", "schedule-previous", () -> move(-1)), date,
                        Ui.action("Next", "schedule-next", () -> move(1)),
                        Ui.action("Today", "schedule-today", () -> date.setValue(today())),
                        Ui.action("List view", "schedule-list", list)),
                Ui.actions(provider, Ui.action("Clear filter", "schedule-clear", () -> provider.setValue(null)), cancelled,
                        Ui.action("Refresh", "schedule-refresh", this::refresh)), title, legend, advice, timeline);
        refresh();
    }
    private LocalDate today() { return controller.time().now().atZone(controller.time().zone()).toLocalDate(); }
    private void move(int direction) {
        date.setValue(CalendarModel.navigate(date.getValue() == null ? today() : date.getValue(), view, direction));
    }
    private void selectDay(LocalDate day) {
        date.setValue(day);
        views.getToggles().stream().filter(t -> t.getUserData() == CalendarModel.View.DAY).findFirst().ifPresent(views::selectToggle);
    }
    void reload() {
        var selectedId = provider.getValue() == null ? null : provider.getValue().id();
        reloading = true;
        try {
            provider.getItems().setAll(controller.providers());
            provider.setValue(provider.getItems().stream().filter(p -> p.id().equals(selectedId)).findFirst().orElse(null));
        } finally { reloading = false; }
        refresh();
    }
    void rememberFocus() {
        var focused = getScene() == null ? null : getScene().getFocusOwner();
        focusId = focused != null && focused.getId() != null && focused.getId().startsWith("schedule-") ? focused.getId() : null;
    }
    void restoreFocus() {
        var target = focusId == null ? null : lookup("#" + focusId);
        if (target != null) target.requestFocus();
    }
    private void refresh() {
        if (reloading || date.getValue() == null) return;
        var selected = date.getValue();
        for (var node : List.of(heading, legend, advice)) {
            node.setVisible(view != CalendarModel.View.MONTH);
            node.setManaged(view != CalendarModel.View.MONTH);
        }
        var range = CalendarModel.range(selected, view);
        var zone = controller.time().zone();
        var all = controller.appointments("", null, null, null);
        var providerId = provider.getValue() == null ? null : provider.getValue().id();
        var grouped = CalendarModel.group(all, range, zone, providerId, cancelled.isSelected());
        long hidden = CalendarModel.group(all, range, zone, providerId, true).values().stream().flatMap(List::stream)
                .filter(a -> a.status() == AppointmentStatus.CANCELLED).count();
        count.setText(grouped.values().stream().mapToInt(List::size).sum() + " shown · " + hidden + " cancelled"
                + (cancelled.isSelected() ? " included" : " hidden"));
        var format = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
        title.setText(switch (view) {
            case DAY -> selected.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH));
            case WEEK -> range.start().format(format) + " – " + range.endExclusive().minusDays(1).format(format);
            case MONTH -> selected.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        });
        timeline.getChildren().clear();
        switch (view) {
            case DAY -> {
                var entries = grouped.get(selected);
                if (entries.isEmpty()) timeline.getChildren().add(Ui.text("A clear day\nNo visible appointments match this date and provider.", "empty-state"));
                else timeline.getChildren().add(new TimeGrid(entries, selected, zone, details));
            }
            case WEEK -> renderWeek(grouped);
            case MONTH -> renderMonth(grouped);
        }
    }
    private void markDate(javafx.scene.Node node, LocalDate day) {
        if (day.equals(date.getValue())) node.getStyleClass().add("calendar-selected-day");
        if (day.equals(today())) node.getStyleClass().add("calendar-today");
    }
    private void renderWeek(Map<LocalDate, List<Appointment>> grouped) {
        var columns = new HBox(0);
        columns.setId("schedule-week-grid");
        var zone = controller.time().zone();
        var entries = grouped.values().stream().flatMap(List::stream).toList();
        int firstHour = Math.min(9, entries.stream().mapToInt(a -> a.slot().start().atZone(zone).getHour()).min().orElse(9));
        int endHour = Math.max(17, entries.stream().mapToInt(a -> {
            var end = a.slot().end().atZone(zone);
            return end.getHour() + (end.getMinute() > 0 || end.getSecond() > 0 ? 1 : 0);
        }).max().orElse(17));
        grouped.forEach((day, appointments) -> {
            var heading = Ui.action(day.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH))
                    + (day.equals(today()) ? " · Today" : ""), "schedule-week-day-" + day, () -> selectDay(day));
            heading.setMaxWidth(Double.MAX_VALUE);
            markDate(heading, day);
            var grid = new TimeGrid(appointments, day, zone, details,
                    day.atTime(firstHour, 0).atZone(zone).toInstant(),
                    day.atStartOfDay().plusHours(endHour).atZone(zone).toInstant(),
                    day.equals(grouped.keySet().iterator().next()));
            grid.setId("schedule-week-grid-" + day);
            int lanes = CalendarModel.layout(appointments).stream().mapToInt(CalendarModel.Placement::lanes).max().orElse(1);
            var column = new VBox(6, heading, Ui.label(appointments.isEmpty() ? "No appointments" : appointments.size() + " appointments", "muted"), grid);
            column.getStyleClass().add("calendar-week-column");
            column.setMinWidth(grid.gutter + lanes * 100 + 12);
            column.setPrefWidth(column.getMinWidth());
            column.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(column, Priority.ALWAYS);
            columns.getChildren().add(column);
        });
        var horizontal = new ScrollPane(columns);
        horizontal.setId("schedule-week-scroll");
        horizontal.setFitToWidth(true);
        horizontal.setFitToHeight(true);
        horizontal.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontal.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        horizontal.setHvalue(weekScroll);
        horizontal.hvalueProperty().addListener((o, a, b) -> weekScroll = b.doubleValue());
        // The shell owns vertical scrolling; this viewport only scrolls horizontally.
        horizontal.setMinHeight(Region.USE_PREF_SIZE);
        horizontal.getStyleClass().add("calendar-week-scroll");
        var pan = new ScrollBar();
        pan.setId("schedule-week-pan");
        pan.setAccessibleText("Scroll week horizontally");
        pan.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        pan.setMin(0); pan.setMax(1); pan.setUnitIncrement(0.1); pan.setBlockIncrement(0.5);
        pan.valueProperty().bindBidirectional(horizontal.hvalueProperty());
        pan.visibleProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> columns.getWidth() > horizontal.getViewportBounds().getWidth() + 1,
                columns.widthProperty(), horizontal.viewportBoundsProperty()));
        pan.managedProperty().bind(pan.visibleProperty());
        var earlier = Ui.action("Earlier days", "schedule-week-left", () -> horizontal.setHvalue(Math.max(0, horizontal.getHvalue() - 0.25)));
        var later = Ui.action("Later days", "schedule-week-right", () -> horizontal.setHvalue(Math.min(1, horizontal.getHvalue() + 0.25)));
        earlier.disableProperty().bind(horizontal.hvalueProperty().lessThanOrEqualTo(0));
        later.disableProperty().bind(horizontal.hvalueProperty().greaterThanOrEqualTo(1));
        var scrollHelp = Ui.text("More days across the week — use these buttons or the scrollbar.", "muted");
        var affordance = Ui.actions(earlier, later, scrollHelp);
        affordance.setId("schedule-week-affordance");
        affordance.visibleProperty().bind(pan.visibleProperty());
        affordance.managedProperty().bind(affordance.visibleProperty());
        timeline.getChildren().addAll(Ui.text("Select a day heading to open Day view.", "muted"), affordance, pan, horizontal);
    }
    private void renderMonth(Map<LocalDate, List<Appointment>> grouped) {
        var grid = new GridPane();
        grid.setId("schedule-month-grid");
        grid.setHgap(3); grid.setVgap(3);
        for (int col = 0; col < 7; col++) {
            var constraint = new ColumnConstraints(); constraint.setPercentWidth(100.0 / 7); constraint.setMinWidth(0);
            grid.getColumnConstraints().add(constraint);
            grid.add(Ui.label(DayOfWeek.of(col + 1).getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH), "field-label"), col, 0);
        }
        int index = 0;
        for (var entry : grouped.entrySet()) {
            var day = entry.getKey(); var appointments = entry.getValue();
            long cancelledCount = appointments.stream().filter(a -> a.status() == AppointmentStatus.CANCELLED).count();
            String dayLabel = day.getDayOfMonth() == 1 || !day.getMonth().equals(date.getValue().getMonth())
                    ? day.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)) : Integer.toString(day.getDayOfMonth());
            var content = new VBox(3, Ui.label(dayLabel + (day.equals(today()) ? " · Today" : ""), "row-primary"),
                    Ui.label((appointments.size() - cancelledCount) + " bookings", "month-active-count"));
            if (cancelledCount > 0) content.getChildren().add(Ui.label(cancelledCount + " cancelled", "month-cancelled-count"));
            content.setMouseTransparent(true); content.setMinWidth(0);
            var cell = Ui.action("", "schedule-month-day-" + day, () -> selectDay(day));
            cell.setGraphic(content); cell.setMinWidth(0); cell.setMaxWidth(Double.MAX_VALUE); cell.setPrefHeight(64);
            content.maxWidthProperty().bind(cell.widthProperty().subtract(18));
            cell.getStyleClass().add("calendar-month-cell");
            if (day.getMonth() != date.getValue().getMonth()) cell.getStyleClass().add("calendar-outside-month");
            markDate(cell, day);
            cell.setAccessibleText(day + ", " + (appointments.size() - cancelledCount) + " non-cancelled, " + cancelledCount + " cancelled. Open day schedule.");
            cell.setTooltip(new Tooltip(cell.getAccessibleText()));
            cell.setFocusTraversable(day.equals(date.getValue()));
            cell.setAccessibleHelp("Arrow keys move by day or week. Home and End move within the week. Page Up and Page Down change month. Enter or Space opens the day.");
            cell.setOnKeyPressed(e -> {
                var next = CalendarKeyboard.move(day, e.getCode());
                if (next.equals(day)) return;
                e.consume();
                date.setValue(next);
                javafx.application.Platform.runLater(() -> {
                    if (view != CalendarModel.View.MONTH || getScene() == null) return;
                    applyCss(); layout();
                    var target = lookup("#schedule-month-day-" + next);
                    if (target == null) return;
                    target.requestFocus();
                    // Keep the focused cell inside the shell viewport, including the last compact row.
                    for (var parent = getParent(); parent != null; parent = parent.getParent()) {
                        if (parent instanceof ScrollPane outer) {
                            var bounds = target.localToScene(target.getBoundsInLocal());
                            var viewport = outer.lookup(".viewport");
                            if (viewport == null) break;
                            var visible = viewport.localToScene(viewport.getBoundsInLocal());
                            double overflow = outer.getContent().getLayoutBounds().getHeight() - outer.getViewportBounds().getHeight();
                            double delta = bounds.getMaxY() > visible.getMaxY() ? bounds.getMaxY() - visible.getMaxY()
                                    : Math.min(0, bounds.getMinY() - visible.getMinY());
                            if (overflow > 0) outer.setVvalue(Math.clamp(outer.getVvalue() + delta / overflow, 0, 1));
                            break;
                        }
                    }
                });
            });
            grid.add(cell, index % 7, 1 + index / 7); index++;
        }
        timeline.getChildren().addAll(Ui.text("Arrow keys move between dates · Page Up / Down changes month · Enter opens the day", "muted"), grid);
    }

    /** Positions use Instants, so an offset change never distorts elapsed service duration. */
    private static final class TimeGrid extends Pane {
        private static final double PIXELS_PER_MINUTE = 1.5;
        private final double gutter;
        private static final double TOP = 12;
        private final List<Block> blocks = new ArrayList<>();
        private final List<Region> lines = new ArrayList<>();
        private final Instant origin;

        TimeGrid(List<Appointment> appointments, LocalDate day, ZoneId zone, Consumer<AppointmentId> details) {
            this(appointments, day, zone, details, null, null, true);
        }
        TimeGrid(List<Appointment> appointments, LocalDate day, ZoneId zone, Consumer<AppointmentId> details,
                 Instant rangeStart, Instant rangeEnd, boolean showAxis) {
            setId("schedule-grid");
            setMinWidth(0);
            // Keep familiar planning hours; extend them for bookings outside that range.
            var first = appointments.stream().map(a -> a.slot().start()).min(Instant::compareTo).orElse(day.atTime(9, 0).atZone(zone).toInstant());
            var last = appointments.stream().map(a -> a.slot().end()).max(Instant::compareTo).orElse(day.atTime(17, 0).atZone(zone).toInstant());
            var defaultStart = day.atTime(9, 0).atZone(zone).toInstant();
            var defaultEnd = day.atTime(17, 0).atZone(zone).toInstant();
            origin = rangeStart != null ? rangeStart : (first.isBefore(defaultStart) ? first : defaultStart).atZone(zone).withMinute(0).withSecond(0).withNano(0).toInstant();
            var end = rangeEnd != null ? rangeEnd : (last.isAfter(defaultEnd) ? last : defaultEnd);
            double height = y(end) + 20;
            setMinHeight(height);
            setPrefHeight(height);
            var clock = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
            boolean offsetChanges = CalendarModel.hasOffsetChange(day, zone)
                    || !origin.atZone(zone).getOffset().equals(end.atZone(zone).getOffset());
            gutter = showAxis || offsetChanges ? 60 : 0;
            for (var tick = origin; !tick.isAfter(end); tick = tick.plusSeconds(1800)) {
                var line = new Region();
                line.getStyleClass().add(tick.atZone(zone).getMinute() == 0 ? "grid-hour-line" : "grid-half-line");
                line.setManaged(false);
                line.setLayoutY(y(tick));
                lines.add(line);
                getChildren().add(line);
                if (gutter > 0 && tick.atZone(zone).getMinute() == 0) {
                    var label = Ui.label(clock.format(tick.atZone(zone)) + (offsetChanges ? "\n" + tick.atZone(zone).getOffset() : ""), "grid-time");
                    label.setManaged(false);
                    label.relocate(0, y(tick) - 8);
                    label.autosize();
                    getChildren().add(label);
                }
            }
            for (var placement : CalendarModel.layout(appointments)) {
                var a = placement.appointment();
                String time = clock.format(a.slot().start().atZone(zone)) + "–" + clock.format(a.slot().end().atZone(zone));
                String summary = time + " · " + a.customer().name() + " · " + DesktopController.title(a.status());
                var button = Ui.action(summary, "schedule-appointment-" + a.id().value(), () -> details.accept(a.id()));
                button.setAccessibleText(summary + ", " + a.service().name() + ", " + a.provider().name());
                button.setTooltip(new Tooltip(button.getAccessibleText()));
                var content = new VBox(2, Ui.label(summary, "grid-entry-title"),
                        Ui.label(a.provider().name() + " · " + a.service().name(), "grid-entry-detail"));
                content.setMinWidth(0);
                content.setMouseTransparent(true);
                button.setText(null);
                button.setGraphic(content);
                button.setManaged(false);
                button.setMinSize(0, 0);
                button.getStyleClass().addAll("schedule-entry", "schedule-" + a.status().name().toLowerCase(Locale.ROOT));
                var block = new Block(button, a, placement.lane());
                block.lanes = placement.lanes();
                blocks.add(block);
                getChildren().add(button);
            }
        }
        private double y(Instant instant) { return TOP + Duration.between(origin, instant).toMillis() / 60000.0 * PIXELS_PER_MINUTE; }
        @Override protected void layoutChildren() {
            for (var child : getChildren()) if (child instanceof Label label) label.autosize();
            for (var line : lines) line.resizeRelocate(gutter, line.getLayoutY(), Math.max(0, getWidth() - gutter), 1);
            for (var b : blocks) {
                double width = Math.max(0, getWidth() - gutter) / b.lanes;
                double height = Duration.between(b.appointment.slot().start(), b.appointment.slot().end()).toMillis() / 60000.0 * PIXELS_PER_MINUTE;
                b.button.resizeRelocate(gutter + b.lane * width, y(b.appointment.slot().start()), Math.max(0, width - 4), height);
                var content = (VBox) b.button.getGraphic();
                content.setMaxWidth(Math.max(0, width - 24));
                var detail = content.getChildren().get(1);
                detail.setVisible(height >= 42);
                detail.setManaged(height >= 42);
            }
        }
        private static final class Block {
            final Button button;
            final Appointment appointment;
            final int lane;
            int lanes;
            Block(Button button, Appointment appointment, int lane) { this.button = button; this.appointment = appointment; this.lane = lane; }
        }
    }
}
