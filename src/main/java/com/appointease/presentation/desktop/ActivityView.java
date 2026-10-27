package com.appointease.presentation.desktop;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

final class ActivityView extends VBox {
    ActivityView(DesktopController controller) {
        getStyleClass().add("page");
        setSpacing(16);
        var channel = Ui.combo("activity-channel", List.of("All records", "Audit", "Notification (simulated)"), s -> s);
        channel.getSelectionModel().selectFirst();
        var records = new VBox(10);
        var feedback = Ui.text("", "muted");
        Runnable refresh = () -> {
            records.getChildren().clear();
            feedback.setText("");
            try {
                var items = controller.activity().stream().filter(a -> "All records".equals(channel.getValue()) || a.channel().equals(channel.getValue())).toList();
                if (items.isEmpty()) records.getChildren().add(Ui.text("No activity yet. Successful booking and lifecycle actions appear here.", "empty-state"));
                for (var record : items) {
                    var e = record.event();
                    var eventType = Ui.label(DesktopController.title(e.type()), "event-type");
                    var row = new VBox(7, Ui.actions(eventType, Ui.text(controller.format(e.occurredAt()), "event-timestamp")),
                            Ui.text(e.after().customer().name() + " · " + e.after().service().name() + " · " + e.after().provider().name(), "row-secondary"),
                            Ui.text("Scheduled " + controller.format(e.after().slot().start()), "muted"),
                            Ui.text("Booking " + e.after().id().value(), "event-reference"),
                            Ui.text(record.channel() + " · Revision " + e.revision(), "muted"));
                    Tooltip.install(row, new Tooltip("Event " + e.id()));
                    row.getStyleClass().add("history-row");
                    records.getChildren().add(row);
                }
                for (var warning : controller.warnings()) records.getChildren().add(Ui.text("Saved with delivery warning · "
                        + warning.consumer() + "\n" + warning.eventId() + " · " + warning.cause().getMessage(), "feedback-error"));
                feedback.setText(items.size() + " records · Newest events first");
            } catch (RuntimeException ex) { Ui.error(feedback, ex); }
        };
        channel.valueProperty().addListener((o, a, b) -> refresh.run());
        getChildren().setAll(Ui.label("Activity", "page-title"),
                Ui.text("Notification and audit history · Notifications are simulated, with no email or SMS delivery.", "subtitle"),
                Ui.actions(Ui.field("Record type", channel), Ui.action("Refresh", "activity-refresh", refresh)), feedback, records);
        refresh.run();
    }
}
