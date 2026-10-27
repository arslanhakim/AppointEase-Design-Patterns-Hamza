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
    ScheduleView(DesktopController controller, LocalDate initialDay, Consumer<AppointmentId> details, Runnable list) {
        getStyleClass().add("page");
        setSpacing(12);
        var date = Ui.date("schedule-date", initialDay);
        var provider = Ui.combo("schedule-provider", controller.providers(), ServiceProvider::name);
        provider.setPromptText("All providers");
        var cancelled = new CheckBox("Show cancelled");
        cancelled.setId("schedule-show-cancelled");
        var timeline = new VBox(8);
        timeline.setId("schedule-timeline");
        var title = Ui.text("", "section-title");
        var count = Ui.text("", "muted");
        Runnable refresh = () -> {
            if (date.getValue() == null) return;
            title.setText(date.getValue().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)));
            var appointments = controller.appointments("", null, provider.getValue() == null ? null : provider.getValue().id(), date.getValue());
            var shown = appointments.stream().filter(a -> cancelled.isSelected() || a.status() != AppointmentStatus.CANCELLED).toList();
            count.setText(shown.size() + " shown · " + appointments.stream().filter(a -> a.status() == AppointmentStatus.CANCELLED).count()
                    + " cancelled" + (cancelled.isSelected() ? " included" : " hidden"));
            timeline.getChildren().clear();
            if (shown.isEmpty()) timeline.getChildren().add(Ui.text("A clear day\nNo visible appointments match this date and provider.", "empty-state"));
            else timeline.getChildren().add(new TimeGrid(shown, date.getValue(), controller.time().zone(), details));
        };
        date.valueProperty().addListener((o, a, b) -> refresh.run());
        provider.valueProperty().addListener((o, a, b) -> refresh.run());
        cancelled.selectedProperty().addListener((o, a, b) -> refresh.run());
        getChildren().setAll(Ui.label("Schedule", "page-title"),
                Ui.text("Plan the day · " + controller.time().zone() + " · Blocks follow appointment start and duration", "subtitle"),
                Ui.actions(Ui.action("Previous", "schedule-previous", () -> date.setValue((date.getValue() == null ? initialDay : date.getValue()).minusDays(1))), date,
                        Ui.action("Next", "schedule-next", () -> date.setValue((date.getValue() == null ? initialDay : date.getValue()).plusDays(1))),
                        Ui.action("Today", "schedule-today", () -> date.setValue(controller.time().now().atZone(controller.time().zone()).toLocalDate())),
                        Ui.action("List view", "schedule-list", list)),
                Ui.actions(provider, Ui.action("Clear filter", "schedule-clear", () -> provider.setValue(null)), cancelled,
                        Ui.action("Refresh", "schedule-refresh", refresh)), title,
                Ui.actions(Ui.status(AppointmentStatus.PENDING), Ui.status(AppointmentStatus.CONFIRMED),
                        Ui.status(AppointmentStatus.COMPLETED), Ui.status(AppointmentStatus.CANCELLED), count),
                Ui.text("Open a block for full details. Empty space does not guarantee availability.", "muted"), timeline);
        refresh.run();
    }

    /** Positions use Instants, so an offset change never distorts elapsed service duration. */
    private static final class TimeGrid extends Pane {
        private static final double PIXELS_PER_MINUTE = 1.5;
        private static final double GUTTER = 60;
        private static final double TOP = 12;
        private final List<Block> blocks = new ArrayList<>();
        private final List<Region> lines = new ArrayList<>();
        private final Instant origin;

        TimeGrid(List<Appointment> appointments, LocalDate day, ZoneId zone, Consumer<AppointmentId> details) {
            setId("schedule-grid");
            setMinWidth(0);
            // Keep familiar planning hours; extend them for bookings outside that range.
            var first = appointments.stream().map(a -> a.slot().start()).min(Instant::compareTo).orElseThrow();
            var last = appointments.stream().map(a -> a.slot().end()).max(Instant::compareTo).orElseThrow();
            var defaultStart = day.atTime(9, 0).atZone(zone).toInstant();
            var defaultEnd = day.atTime(17, 0).atZone(zone).toInstant();
            origin = (first.isBefore(defaultStart) ? first : defaultStart).atZone(zone).withMinute(0).withSecond(0).withNano(0).toInstant();
            var end = last.isAfter(defaultEnd) ? last : defaultEnd;
            double height = y(end) + 20;
            setMinHeight(height);
            setPrefHeight(height);
            var clock = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
            boolean offsetChanges = !origin.atZone(zone).getOffset().equals(end.atZone(zone).getOffset());
            for (var tick = origin; !tick.isAfter(end); tick = tick.plusSeconds(1800)) {
                var line = new Region();
                line.getStyleClass().add(tick.atZone(zone).getMinute() == 0 ? "grid-hour-line" : "grid-half-line");
                line.setManaged(false);
                line.setLayoutY(y(tick));
                lines.add(line);
                getChildren().add(line);
                if (tick.atZone(zone).getMinute() == 0) {
                    var label = Ui.label(clock.format(tick.atZone(zone)) + (offsetChanges ? "\n" + tick.atZone(zone).getOffset() : ""), "grid-time");
                    label.setManaged(false);
                    label.relocate(0, y(tick) - 8);
                    label.autosize();
                    getChildren().add(label);
                }
            }
            // Partition connected overlaps. Adjacency reuses a lane; separate clusters get full width.
            var sorted = appointments.stream().sorted(Comparator.comparing((Appointment a) -> a.slot().start()).thenComparing(a -> a.id().value())).toList();
            var cluster = new ArrayList<Block>();
            var laneEnds = new ArrayList<Instant>();
            Instant clusterEnd = origin;
            for (var a : sorted) {
                if (!a.slot().start().isBefore(clusterEnd)) {
                    finishCluster(cluster, laneEnds.size());
                    cluster.clear();
                    laneEnds.clear();
                }
                int lane = 0;
                while (lane < laneEnds.size() && laneEnds.get(lane).isAfter(a.slot().start())) lane++;
                if (lane == laneEnds.size()) laneEnds.add(a.slot().end());
                else laneEnds.set(lane, a.slot().end());
                if (a.slot().end().isAfter(clusterEnd)) clusterEnd = a.slot().end();
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
                var block = new Block(button, a, lane);
                cluster.add(block);
                blocks.add(block);
                getChildren().add(button);
            }
            finishCluster(cluster, laneEnds.size());
        }
        private void finishCluster(List<Block> cluster, int lanes) { cluster.forEach(b -> b.lanes = lanes); }
        private double y(Instant instant) { return TOP + Duration.between(origin, instant).toMillis() / 60000.0 * PIXELS_PER_MINUTE; }
        @Override protected void layoutChildren() {
            for (var child : getChildren()) if (child instanceof Label label) label.autosize();
            for (var line : lines) line.resizeRelocate(GUTTER, line.getLayoutY(), Math.max(0, getWidth() - GUTTER), 1);
            for (var b : blocks) {
                double width = Math.max(0, getWidth() - GUTTER) / b.lanes;
                double height = Duration.between(b.appointment.slot().start(), b.appointment.slot().end()).toMillis() / 60000.0 * PIXELS_PER_MINUTE;
                b.button.resizeRelocate(GUTTER + b.lane * width, y(b.appointment.slot().start()), Math.max(0, width - 4), height);
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
