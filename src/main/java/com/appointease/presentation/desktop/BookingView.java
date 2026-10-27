package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/** Selection, review and receipt; a reviewed request retains its identity through submission retries. */
final class BookingView extends VBox {
    private final DesktopController controller;
    private final Consumer<AppointmentId> openDetails;
    private final ComboBox<Customer> customer;
    private final ComboBox<ServiceProvider> provider;
    private final ComboBox<Service> service;
    private final ComboBox<AppointmentMode> mode;
    private final DatePicker date;
    private final TextField time = new TextField("14:00");
    private final CheckBox priority = new CheckBox("Priority handling");
    private final CheckBox reminder = new CheckBox("Simulated reminder");
    private final CheckBox buffered = new CheckBox("Include provider preparation buffer");
    private final VBox price = new VBox(6);
    private final Label feedback = Ui.feedback("booking-feedback");
    private final VBox form;
    private final GridPane fields = new GridPane();
    private final List<VBox> inputs;
    private int fieldColumns;
    private DesktopController.PreparedBooking prepared;
    private boolean completed;
    boolean completed() { return completed; }

    BookingView(DesktopController controller, LocalDate day, ServiceProvider selected, Consumer<AppointmentId> openDetails) {
        this.controller = controller;
        this.openDetails = openDetails;
        setSpacing(12);
        getStyleClass().add("page");
        customer = Ui.combo("booking-customer", controller.customers(), Customer::name);
        provider = Ui.combo("booking-provider", controller.providers(), ServiceProvider::name);
        service = Ui.combo("booking-service", List.of(), Service::name);
        mode = Ui.combo("booking-mode", List.of(), DesktopController::title);
        date = Ui.date("booking-date", day);
        time.setId("booking-time");
        priority.setId("booking-priority");
        reminder.setId("booking-reminder");
        buffered.setId("booking-buffered");
        feedback.setId("booking-feedback");
        customer.setPromptText("Select a customer");
        inputs = List.of(Ui.field("Customer", customer), Ui.field("Provider", provider),
                Ui.field("Service", service), Ui.field("Mode", mode), Ui.field("Date", date), Ui.field("Start time · HH:mm", time));
        fields.setHgap(20);
        fields.setVgap(10);
        var options = new VBox(5, Ui.text("02  /  Optional services", "eyebrow"), Ui.actions(priority, reminder));
        options.getStyleClass().add("optional-services");
        price.setId("booking-price");
        form = new VBox(10, Ui.text("01  /  Appointment details", "eyebrow"), fields, options, buffered,
                price, Ui.text("Times use " + controller.time().zone() + ". Availability is checked again when you book.\n"
                        + "During a repeated clock hour, the earlier instant is used; review the UTC offset before booking.", "muted"),
                Ui.actions(Ui.action("Check availability", "booking-check", this::check),
                        Ui.primary("Review appointment", "booking-review", this::review)));
        provider.valueProperty().addListener((o, old, value) -> {
            service.getItems().setAll(controller.services(value));
            service.getSelectionModel().selectFirst();
            mode.getItems().setAll(value == null ? List.of() : value.modes().stream().sorted().toList());
            mode.getSelectionModel().selectFirst();
            quote();
        });
        service.valueProperty().addListener((o, old, value) -> quote());
        priority.selectedProperty().addListener((o, old, value) -> quote());
        reminder.selectedProperty().addListener((o, old, value) -> quote());
        provider.setValue(selected == null ? controller.providers().getFirst() : selected);
        widthProperty().addListener((o, old, width) -> arrange(width.doubleValue()));
        arrange(600);
        edit();
    }

    private void arrange(double width) {
        int columns = width >= 850 ? 3 : width >= 480 ? 2 : 1;
        if (columns == fieldColumns) return;
        fieldColumns = columns;
        fields.getChildren().clear();
        fields.getColumnConstraints().clear();
        for (int i = 0; i < columns; i++) {
            var c = new ColumnConstraints();
            c.setPercentWidth(100.0 / columns);
            c.setMinWidth(0);
            fields.getColumnConstraints().add(c);
        }
        for (int i = 0; i < inputs.size(); i++) fields.add(inputs.get(i), i % columns, i / columns);
    }
    private void edit() {
        prepared = null;
        feedback.setText("");
        getChildren().setAll(Ui.label("New appointment", "page-title"),
                Ui.text("Choose the details, review the price, then reserve your appointment.", "subtitle"), form, feedback);
    }
    private DesktopController.Draft draft() {
        return new DesktopController.Draft(customer.getValue(), provider.getValue(), service.getValue(), date.getValue(),
                time.getText(), mode.getValue(), priority.isSelected(), reminder.isSelected(), buffered.isSelected());
    }
    private void quote() {
        if (service.getValue() == null) { price.getChildren().setAll(Ui.text("Choose a service to see its price.", "muted")); return; }
        var quote = controller.quote(service.getValue(), priority.isSelected(), reminder.isSelected());
        price.getChildren().setAll(Ui.priceSummary(quote), Ui.text(service.getValue().duration().toMinutes()
                + " minutes · No payment collected", "muted"));
    }
    private void check() {
        try {
            controller.prepare(draft());
            feedback.getStyleClass().setAll("feedback-success");
            feedback.setText("This time is currently available. Review the appointment to continue; no reservation has been made.");
        } catch (RuntimeException ex) { Ui.error(feedback, ex); }
    }
    private void review() {
        try {
            prepared = controller.prepare(draft());
            var d = prepared.draft();
            feedback.setText("");
            var summary = new VBox(8, Ui.text("03  / Review & reserve", "eyebrow"),
                    Ui.appointmentWhen(prepared.request().start(), prepared.request().start().plus(d.service().duration()),
                            d.provider().name(), controller.time().zone()),
                    Ui.text(d.customer().name() + " · " + d.service().name(), "row-primary"),
                    Ui.text(d.service().duration().toMinutes() + " minutes · " + DesktopController.title(d.mode())
                            + "\n" + (d.buffered() ? "Provider preparation buffer included" : "Standard availability policy"), "row-secondary"),
                    Ui.priceSummary(prepared.quote()),
                    Ui.text(String.join("\n", prepared.quote().instructions()), "muted"),
                    Ui.text("Booking reserves this time with Pending status. Confirm the appointment from its details when ready.", "muted"));
            summary.getStyleClass().add("detail-panel");
            getChildren().setAll(Ui.label("Review appointment", "page-title"), summary,
                    Ui.actions(Ui.action("Back to edit", "booking-edit", this::edit),
                            Ui.primary("Book appointment", "booking-submit", this::submit)), feedback);
        } catch (RuntimeException ex) { Ui.error(feedback, ex); }
    }
    private void submit() {
        var submit = (Button) lookup("#booking-submit");
        if (submit == null || submit.isDisabled()) return;
        submit.setDisable(true);
        try {
            var result = controller.book(prepared);
            completed = true;
            var a = result.appointment();
            var receipt = new VBox(12, Ui.actions(Ui.text("RESERVATION SAVED", "eyebrow"), Ui.status(a.status())),
                    Ui.appointmentWhen(a.slot().start(), a.slot().end(), a.provider().name(), controller.time().zone()),
                    Ui.text(a.customer().name() + " · " + a.service().name(), "row-primary"),
                    Ui.text(DesktopController.title(a.mode()) + " · " + a.service().duration().toMinutes() + " minutes", "row-secondary"),
                    Ui.priceTotal(a.totalFee()),
                    Ui.text("Booking reference\n" + a.id().value(), "muted"));
            receipt.setId("booking-receipt");
            receipt.getStyleClass().add("detail-panel");
            getChildren().setAll(Ui.label("Appointment booked", "page-title"),
                    Ui.text("Your time is reserved. You can now confirm or manage this appointment.", "subtitle"), receipt);
            if (!result.warnings().isEmpty()) getChildren().add(Ui.text(
                    "Saved with delivery warnings. See Activity for details; do not book again.", "feedback-error"));
            getChildren().add(Ui.primary("View appointment", "booking-open-details", () -> openDetails.accept(a.id())));
        } catch (RuntimeException ex) { Ui.error(feedback, ex); submit.setDisable(false); }
    }
}
