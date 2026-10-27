package com.appointease.presentation.desktop;

import com.appointease.domain.model.AppointmentId;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.UUID;

/** Explicit confirmation panels; every mutation re-enters the facade with a stable command identity. */
final class AppointmentDetailsView extends VBox {
    private final DesktopController controller;
    private final AppointmentId id;
    private final VBox operation = new VBox(12);
    private final Label feedback = Ui.feedback("details-feedback");

    AppointmentDetailsView(DesktopController controller, AppointmentId id, Runnable back) {
        this.controller = controller;
        this.id = id;
        setSpacing(18);
        getStyleClass().add("page");
        feedback.setId("details-feedback");
        operation.visibleProperty().bind(javafx.beans.binding.Bindings.isNotEmpty(operation.getChildren()));
        operation.managedProperty().bind(operation.visibleProperty());
        render(back);
    }
    private void render(Runnable back) {
        var a = controller.details(id);
        operation.getChildren().clear();
        var panel = new VBox(12, Ui.status(a.status()),
                Ui.appointmentWhen(a.slot().start(), a.slot().end(), a.provider().name(), controller.time().zone()),
                Ui.text(a.customer().name() + " · " + a.service().name(), "row-primary"),
                Ui.text(DesktopController.title(a.mode()) + " · " + DesktopController.money(a.totalFee())
                        + "\n" + a.modeDetails().meeting().text() + "\n" + a.modeDetails().instructions().text(), "row-secondary"),
                Ui.text("Add-ons: " + (a.options().additionalFees().isEmpty() ? "None" : a.options().additionalFees().keySet())
                        + "\nReference: " + a.id().value(), "muted"));
        panel.getStyleClass().add("detail-panel");
        panel.setId("appointment-detail-panel");
        var actions = Ui.actions();
        for (var action : DesktopController.Action.values()) {
            var button = Ui.action(DesktopController.title(action), "details-" + action.name().toLowerCase(java.util.Locale.ROOT),
                    () -> {
                        if (action == DesktopController.Action.RESCHEDULE) reschedule(back);
                        else confirm(action, back);
                    });
            button.setDisable(!controller.can(action, a));
            if (!button.isDisabled() && (action == DesktopController.Action.CONFIRM || action == DesktopController.Action.COMPLETE))
                button.getStyleClass().add("primary-button");
            if (action == DesktopController.Action.CANCEL) button.getStyleClass().add("danger-button");
            if (button.isDisabled()) button.setTooltip(new Tooltip("Unavailable for this appointment's current state or time."));
            actions.getChildren().add(button);
        }
        var history = new VBox(10, Ui.label("Appointment history", "section-title"));
        for (var event : controller.history(id)) {
            history.getChildren().add(Ui.text(DesktopController.title(event.type()) + " · Revision " + event.revision()
                    + " · " + controller.format(event.occurredAt()) + "\nScheduled for "
                    + controller.format(event.after().slot().start()), "history-row"));
        }
        getChildren().setAll(Ui.actions(Ui.action("Back to appointments", "details-back", back),
                        Ui.action("Refresh details", "details-refresh", () -> render(back))),
                Ui.label("Appointment details", "page-title"), panel, actions, operation, feedback, history);
    }
    private void confirm(DesktopController.Action action, Runnable back) {
        String commandId = UUID.randomUUID().toString();
        feedback.setText("");
        operation.getStyleClass().setAll("confirmation-panel");
        operation.getChildren().setAll(Ui.text(DesktopController.title(action) + " this appointment?", "section-title"),
                Ui.text(action == DesktopController.Action.CANCEL ? "This releases the reserved time. The appointment remains in history."
                        : "The current appointment will be updated and recorded in Activity.", "muted"),
                Ui.actions(Ui.primary("Yes, " + DesktopController.title(action).toLowerCase(java.util.Locale.ROOT), "details-accept", () -> {
                    try { success(controller.act(action, commandId, id), back); }
                    catch (RuntimeException ex) { Ui.error(feedback, ex); }
                }), Ui.action("Keep unchanged", "details-dismiss", () -> operation.getChildren().clear())));
    }
    private void reschedule(Runnable back) {
        var a = controller.details(id);
        var local = a.slot().start().atZone(controller.time().zone());
        var date = Ui.date("reschedule-date", local.toLocalDate());
        var time = new TextField(local.toLocalTime().toString());
        time.setId("reschedule-time");
        var buffer = new CheckBox("Use provider preparation buffer for the new reservation");
        buffer.setId("reschedule-buffer");
        var explanation = Ui.text("Choose the availability policy for the replacement reservation. The original time stays reserved if this change fails.", "muted");
        feedback.setText("");
        operation.getStyleClass().setAll("confirmation-panel");
        operation.getChildren().setAll(Ui.label("Reschedule appointment", "section-title"), explanation,
                Ui.actions(Ui.field("New date", date), Ui.field("Start time · HH:mm", time)), buffer,
                Ui.primary("Review new time", "reschedule-review", () -> {
                    try {
                        var move = controller.prepareMove(id, date.getValue(), time.getText(), buffer.isSelected());
                        operation.getChildren().setAll(Ui.text("Move to " + controller.format(move.start()) + "?", "section-title"),
                                explanation, Ui.actions(Ui.primary("Save new time", "reschedule-submit", () -> {
                                    try { success(controller.reschedule(move), back); }
                                    catch (RuntimeException ex) { Ui.error(feedback, ex); }
                                }), Ui.action("Edit new time", "reschedule-edit", () -> reschedule(back))));
                    } catch (RuntimeException ex) { Ui.error(feedback, ex); }
                }));
    }
    private void success(com.appointease.application.BookingResult result, Runnable back) {
        render(back);
        feedback.getStyleClass().setAll(result.warnings().isEmpty() ? "feedback-success" : "feedback-error");
        feedback.setText(result.warnings().isEmpty() ? "Appointment updated. All views will show the saved state."
                : "Appointment saved with delivery warnings. See Activity; do not repeat the operation.");
    }
}
