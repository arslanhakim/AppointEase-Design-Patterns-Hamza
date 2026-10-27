package com.appointease.presentation.desktop;

import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.AppointmentMode;
import com.appointease.domain.model.AppointmentStatus;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.converter.LocalDateStringConverter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import com.appointease.domain.model.AppointmentId;
import javafx.scene.input.KeyCode;

/** Read-only daily overview with native date navigation and a proportional agenda. */
public final class OverviewView extends VBox {
    private final OverviewModel model;
    private final DatePicker date;
    private final Label subtitle = Ui.label("", "subtitle");
    private final HBox metrics = new HBox();
    private final GridPane body = new GridPane();
    private final VBox appointments = new VBox();
    private final VBox agenda = new VBox(14);
    private final Label feedback = Ui.label("", "muted");
    private Consumer<AppointmentId> openDetails = id -> { };

    public LocalDate selectedDate() { return date.getValue(); }

    public void setActions(Runnable create, Runnable list, Runnable schedule, Consumer<AppointmentId> details) {
        this.openDetails = details;
        var title = (VBox) getChildren().getFirst();
        var heading = title.getChildren().removeFirst();
        var titleRow = new HBox(12, heading, Ui.spacer(), Ui.primary("New Appointment", "overview-new", create));
        titleRow.setAlignment(Pos.CENTER_LEFT);
        title.getChildren().addFirst(titleRow);
        var toolbar = (javafx.scene.layout.FlowPane) getChildren().get(2);
        toolbar.getChildren().addAll(Ui.action("List", "overview-list", list), Ui.action("Schedule", "overview-schedule", schedule));
        refresh();
    }

    public OverviewView(OverviewModel model, LocalDate initialDay) {
        this.model = model;
        date = new DatePicker(initialDay);
        var dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        date.setConverter(new LocalDateStringConverter(dateFormat, dateFormat));
        date.setEditable(false);
        date.setId("overview-date");
        date.setAccessibleText("Choose the overview date");
        date.setPrefWidth(166);
        date.setOnAction(event -> refresh());
        getStyleClass().add("overview");
        setSpacing(16);
        subtitle.setWrapText(true);
        var title = new VBox(8, Ui.label("Daily overview", "page-title"), subtitle);
        var previous = Ui.button("\u2039", "Previous day", () -> date.setValue(date.getValue().minusDays(1)));
        previous.setId("previous-day");
        var next = Ui.button("\u203a", "Next day", () -> date.setValue(date.getValue().plusDays(1)));
        next.setId("next-day");
        var today = Ui.button("Today", "Show today", () -> date.setValue(model.time().now().atZone(model.time().zone()).toLocalDate()));
        today.setId("today");
        var reload = Ui.button("Refresh", "Refresh appointments", () -> {
            refresh();
            feedback.setText("Updated at " + model.time().now().atZone(model.time().zone()).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        reload.setId("refresh");
        var toolbar = Ui.actions(previous, date, next, today, reload);
        metrics.getStyleClass().add("metrics");
        body.setHgap(28);
        body.setVgap(26);
        appointments.setMinWidth(0);
        agenda.setMinWidth(0);
        body.add(appointments, 0, 0);
        body.add(agenda, 1, 0);
        getChildren().addAll(title, metrics, toolbar, body, feedback);
        widthProperty().addListener((obs, old, width) -> arrange(width.doubleValue() < 850));
        arrange(false);
        refresh();
    }

    private void arrange(boolean small) {
        body.getColumnConstraints().clear();
        var left = new ColumnConstraints();
        left.setPercentWidth(small ? 100 : 65);
        body.getColumnConstraints().add(left);
        if (!small) {
            var right = new ColumnConstraints();
            right.setPercentWidth(35);
            body.getColumnConstraints().add(right);
        }
        GridPane.setColumnIndex(agenda, small ? 0 : 1);
        GridPane.setRowIndex(agenda, small ? 1 : 0);
        getStyleClass().removeAll("compact");
        if (small) getStyleClass().add("compact");
    }

    public void refresh() {
        if (date.getValue() == null) return;
        var snapshot = model.load(date.getValue());
        subtitle.setText(snapshot.day().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH))
                + "  /  " + model.time().zone().getId());
        metrics.getChildren().setAll(metric(snapshot.appointments().size(), "Appointments", "On selected day"),
                metric(snapshot.confirmed(), "Confirmed", "Ready in the schedule"),
                metric(snapshot.pending(), "Awaiting confirmation", "Pending appointments"),
                metric(snapshot.providers(), "Providers scheduled", "Across active bookings"));
        renderAppointments(snapshot.appointments());
        renderAgenda(snapshot);
        feedback.setText("All times in " + model.time().zone().getId() + ".  Demonstration data stays on this computer.");
    }

    private VBox metric(long value, String name, String detail) {
        var result = new VBox(5, Ui.label(String.format(Locale.ROOT, "%02d", value), "metric-value"),
                Ui.label(name, "metric-label"), Ui.label(detail, "metric-detail"));
        result.getStyleClass().add("metric");
        result.setMinWidth(0);
        result.setMaxWidth(Double.MAX_VALUE);
        result.setPrefWidth(1);
        result.getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast).forEach(l -> l.setWrapText(true));
        HBox.setHgrow(result, Priority.ALWAYS);
        return result;
    }

    private void renderAppointments(List<Appointment> items) {
        appointments.getChildren().clear();
        var heading = new HBox(Ui.label("Appointments", "section-title"), Ui.spacer(), Ui.label(items.size() + " scheduled", "muted"));
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getStyleClass().add("section-heading");
        appointments.getChildren().add(heading);
        var table = new VBox();
        table.getStyleClass().add("appointment-list");
        table.getChildren().add(row(Ui.label("TIME", "column-label"), Ui.label("APPOINTMENT", "column-label"),
                Ui.label("PROVIDER", "column-label"), Ui.label("STATUS", "column-label"), true));
        if (items.isEmpty()) {
            var empty = new VBox(10, Ui.label("A clear schedule", "empty-title"),
                    Ui.label("No appointments on this date.\nChoose another day to see its schedule.", "muted"));
            empty.getStyleClass().add("empty-state");
            table.getChildren().add(empty);
        }
        for (var appointment : items) {
            var time = new VBox(4, Ui.label(time(appointment, false), "row-primary"),
                    Ui.label(time(appointment, true), "muted"));
            var customer = Ui.label(appointment.customer().name(), "row-primary");
            var service = Ui.label(appointment.service().name(), "row-secondary");
            service.setWrapText(true);
            customer.setWrapText(true);
            var visit = new VBox(5, customer, service);
            var provider = Ui.label(appointment.provider().name(), "row-secondary");
            provider.setWrapText(true);
            var providerBox = new VBox(5, provider, Ui.label(appointment.mode() == AppointmentMode.ONLINE ? "Online" : "In person", "muted"));
            var item = row(time, visit, providerBox, Ui.status(appointment.status()), false);
            item.setAccessibleText(time(appointment, false) + " to " + time(appointment, true) + ", "
                    + appointment.customer().name() + ", " + appointment.service().name() + ", "
                    + appointment.provider().name() + ", " + appointment.status());
            item.setFocusTraversable(true);
            item.setOnMouseClicked(event -> openDetails.accept(appointment.id()));
            item.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    openDetails.accept(appointment.id());
                    event.consume();
                }
            });
            table.getChildren().add(item);
        }
        appointments.getChildren().add(table);
        var next = items.stream().filter(a -> a.status() == AppointmentStatus.PENDING || a.status() == AppointmentStatus.CONFIRMED)
                .filter(a -> a.slot().start().isAfter(model.time().now())).findFirst();
        var note = new VBox(8, Ui.label("NEXT ON THIS DAY", "eyebrow"));
        next.ifPresentOrElse(a -> {
            note.getChildren().add(Ui.label(time(a, false) + "  /  " + a.customer().name(), "next-title"));
            var detail = Ui.label(a.service().name() + " with " + a.provider().name(), "muted");
            detail.setWrapText(true);
            note.getChildren().add(detail);
        }, () -> note.getChildren().add(Ui.label("No upcoming appointments on this day.", "muted")));
        note.getStyleClass().add("next-appointment");
        appointments.getChildren().add(note);
    }

    private GridPane row(Node time, Node visit, Node provider, Node status, boolean header) {
        var row = new GridPane();
        row.setHgap(12);
        double[] widths = {14, 40, 26, 20};
        for (double width : widths) {
            var constraint = new ColumnConstraints();
            constraint.setPercentWidth(width);
            constraint.setMinWidth(0);
            row.getColumnConstraints().add(constraint);
        }
        row.addRow(0, time, visit, provider, status);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(header ? "list-header" : "appointment-row");
        return row;
    }

    private void renderAgenda(OverviewModel.Snapshot snapshot) {
        agenda.getChildren().clear();
        agenda.getChildren().add(Ui.label("Day at a glance", "section-title"));
        var week = new HBox(3);
        var monday = snapshot.day().minusDays(snapshot.day().getDayOfWeek().getValue() - 1);
        for (int i = 0; i < 7; i++) {
            var day = monday.plusDays(i);
            var button = Ui.button(day.format(DateTimeFormatter.ofPattern("EE", Locale.ENGLISH)).substring(0, 1)
                    + "\n" + day.getDayOfMonth(), day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
                    () -> date.setValue(day));
            button.getStyleClass().add("week-day");
            if (day.equals(snapshot.day())) button.getStyleClass().add("week-selected");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMinWidth(0);
            HBox.setHgrow(button, Priority.ALWAYS);
            week.getChildren().add(button);
        }
        agenda.getChildren().add(week);
        var timeline = new Pane();
        timeline.setMinHeight(290);
        timeline.setPrefHeight(290);
        for (int hour = 9; hour <= 17; hour++) {
            var label = Ui.label(String.format(Locale.ROOT, "%02d:00", hour), "timeline-hour");
            label.relocate(0, (hour - 9) * 34);
            var line = new Pane();
            line.getStyleClass().add("timeline-line");
            line.relocate(45, (hour - 9) * 34 + 8);
            line.prefWidthProperty().bind(timeline.widthProperty().subtract(45));
            line.setPrefHeight(1);
            timeline.getChildren().addAll(label, line);
        }
        var active = snapshot.appointments().stream().filter(a -> a.status() != AppointmentStatus.CANCELLED).toList();
        // A separate lane per provider keeps simultaneous visits visible without asserting availability.
        var providers = active.stream().map(a -> a.provider().id()).distinct().toList();
        for (var appointment : active) {
            var start = appointment.slot().start().atZone(model.time().zone());
            double y = ((start.getHour() - 9) * 60 + start.getMinute()) * 34.0 / 60 + 8;
            double height = appointment.service().duration().toMinutes() * 34.0 / 60;
            var block = Ui.label(time(appointment, false) + "  " + appointment.provider().name(), "time-block");
            if (appointment.status() == AppointmentStatus.PENDING) block.getStyleClass().add("time-block-pending");
            block.setMinHeight(height);
            block.setPrefHeight(height);
            block.setMaxHeight(height);
            block.setLayoutY(y);
            // The read-only seed is sequential. Concurrent visits get separate compact lanes.
            boolean overlaps = active.stream().anyMatch(a -> a != appointment && a.slot().overlaps(appointment.slot()));
            int lanes = overlaps ? providers.size() : 1;
            int lane = overlaps ? providers.indexOf(appointment.provider().id()) : 0;
            block.layoutXProperty().bind(timeline.widthProperty().subtract(49).divide(lanes).multiply(lane).add(49));
            block.prefWidthProperty().bind(timeline.widthProperty().subtract(49).divide(lanes).subtract(3));
            block.setMinWidth(0);
            var description = time(appointment, false) + "\u2013" + time(appointment, true) + "\n"
                    + appointment.service().name() + "\n" + appointment.provider().name() + " / " + appointment.customer().name();
            block.setTooltip(new Tooltip(description));
            block.setAccessibleText(description);
            timeline.getChildren().add(block);
        }
        agenda.getChildren().add(timeline);
        var legend = Ui.label("Booked time only.\nEmpty space is not a guarantee of availability.", "muted");
        legend.setWrapText(true);
        agenda.getChildren().add(legend);
        agenda.getStyleClass().setAll("agenda");
    }

    private String time(Appointment a, boolean end) {
        return (end ? a.slot().end() : a.slot().start()).atZone(model.time().zone())
                .format(DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT));
    }
}
