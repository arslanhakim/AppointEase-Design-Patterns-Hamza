package com.appointease.presentation.desktop;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

/** Virtualized chronological feed with the selected immutable event snapshot alongside it. */
final class ActivityView extends VBox {
    private final DesktopController controller;
    private final ListView<DesktopController.Activity> records = new ListView<>();
    private final VBox details = new VBox(8);
    private final Tooltip eventReference = new Tooltip();
    private final GridPane body = new GridPane();
    private final Label feedback = Ui.feedback("activity-feedback");
    private final VBox warnings = new VBox(8);
    private final ComboBox<String> channel;
    private boolean stacked;

    ActivityView(DesktopController controller) {
        this.controller = controller;
        getStyleClass().add("page");
        setSpacing(12); setMinWidth(0);
        channel = Ui.combo("activity-channel", List.of("All records", "Audit", "Notification (simulated)"), s -> s);
        channel.getSelectionModel().selectFirst();
        records.setId("activity-records");
        records.setAccessibleText("Activity events, newest first. Use Up and Down to select an event and inspect its details.");
        records.getStyleClass().add("activity-feed");
        records.setMinSize(0, 220); records.setPrefHeight(340);
        records.setPlaceholder(Ui.text("No activity matches this record type.", "empty-state"));
        records.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(DesktopController.Activity item, boolean empty) {
                super.updateItem(item, empty);
                setText(null); setGraphic(null); setAccessibleText(null);
                if (empty || item == null) return;
                var e = item.event();
                var row = new VBox(5, Ui.label(DesktopController.title(e.type()) + " · " + item.channel(), "event-type"),
                        Ui.label(controller.format(e.occurredAt()), "event-timestamp"),
                        Ui.label(e.after().customer().name() + " · " + e.after().provider().name(), "row-secondary"));
                row.setMinWidth(0); row.setMouseTransparent(true);
                row.prefWidthProperty().bind(widthProperty().subtract(28));
                setGraphic(row);
                setAccessibleText(DesktopController.title(e.type()) + ", " + item.channel() + ", "
                        + controller.format(e.occurredAt()) + ", " + e.after().customer().name() + ", " + e.after().provider().name());
            }
        });
        records.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> inspect(b));
        details.setId("activity-details"); details.setMinWidth(0);
        details.setMinHeight(Region.USE_PREF_SIZE);
        details.getStyleClass().add("activity-details");
        body.setId("activity-body"); body.setHgap(24); body.setVgap(20);
        VBox.setVgrow(body, Priority.ALWAYS);
        widthProperty().addListener((o, a, b) -> arrange(b.doubleValue() < 800));
        arrange(false);
        channel.valueProperty().addListener((o, a, b) -> refresh());
        getChildren().setAll(Ui.label("Activity", "page-title"),
                Ui.text("Notification and audit history · Notifications are simulated, with no email or SMS delivery.", "subtitle"),
                Ui.actions(Ui.field("Record type", channel), Ui.action("Refresh", "activity-refresh", this::refresh)),
                feedback, body, warnings);
        warnings.managedProperty().bind(warnings.visibleProperty());
        refresh();
    }
    private void arrange(boolean compact) {
        if (!body.getChildren().isEmpty() && stacked == compact) return;
        stacked = compact;
        body.getChildren().clear(); body.getColumnConstraints().clear();
        var feedColumn = new ColumnConstraints(); feedColumn.setPercentWidth(compact ? 100 : 62);
        body.getColumnConstraints().add(feedColumn);
        if (!compact) { var detailColumn = new ColumnConstraints(); detailColumn.setPercentWidth(38); body.getColumnConstraints().add(detailColumn); }
        body.add(records, 0, 0); body.add(details, compact ? 0 : 1, compact ? 1 : 0);
        GridPane.setValignment(details, javafx.geometry.VPos.TOP);
        GridPane.setVgrow(records, compact ? Priority.NEVER : Priority.ALWAYS);
        records.setPrefHeight(compact ? 240 : 340);
        records.setMaxHeight(compact ? 240 : Double.MAX_VALUE);
        body.setMinHeight(compact ? Region.USE_PREF_SIZE : Region.USE_COMPUTED_SIZE);
    }
    private void refresh() {
        var selected = records.getSelectionModel().getSelectedItem();
        try {
            var items = controller.activity().stream().filter(a -> "All records".equals(channel.getValue()) || a.channel().equals(channel.getValue())).toList();
            records.getItems().setAll(items);
            // Channel is part of identity: audit and notification share an event ID.
            var retained = selected == null ? null : items.stream().filter(a -> a.channel().equals(selected.channel())
                    && a.event().id().equals(selected.event().id())).findFirst().orElse(null);
            if (retained != null) records.getSelectionModel().select(retained);
            else if (!items.isEmpty()) records.getSelectionModel().selectFirst();
            else inspect(null);
            feedback.getStyleClass().setAll("muted");
            feedback.setAccessibleText(null);
            feedback.setText(items.size() + " records · Newest events first · Select an event to inspect");
            warnings.getChildren().clear();
            for (var warning : controller.warnings()) warnings.getChildren().add(Ui.text("Saved with delivery warning · "
                    + warning.consumer() + "\n" + warning.eventId() + " · " + warning.cause().getMessage(), "feedback-error"));
            warnings.setVisible(!warnings.getChildren().isEmpty());
        } catch (RuntimeException ex) {
            records.getItems().clear(); inspect(null); Ui.error(feedback, ex);
        }
    }
    private void inspect(DesktopController.Activity record) {
        details.getChildren().clear();
        Tooltip.uninstall(details, eventReference);
        details.setAccessibleText(null);
        if (record == null) { details.getChildren().add(Ui.text("Select an event to inspect its details.", "muted")); return; }
        var e = record.event(); var a = e.after();
        details.getChildren().addAll(Ui.label("Event details", "section-title"),
                Ui.text(DesktopController.title(e.type()), "event-type"), Ui.text(controller.format(e.occurredAt()), "event-timestamp"),
                Ui.text(record.channel() + " · Revision " + e.revision(), "muted"), Ui.status(a.status()),
                value("Customer", a.customer().name()), value("Provider", a.provider().name()),
                value("Appointment reference", a.id().value()), value("Service / scheduled time", a.service().name() + "\n" + controller.format(a.slot().start())));
        eventReference.setText("Event " + e.id());
        Tooltip.install(details, eventReference);
        details.setAccessibleText("Selected " + record.channel() + " event " + e.id());
    }
    private VBox value(String label, String value) {
        var field = new VBox(3, Ui.label(label, "field-label"), Ui.text(value, "row-secondary"));
        field.setMinWidth(0); field.setMinHeight(Region.USE_PREF_SIZE); return field;
    }
}
