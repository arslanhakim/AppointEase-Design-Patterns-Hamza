package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Locale;
import java.util.function.Consumer;

final class CustomersView extends VBox {
    private final DesktopController controller;
    private final Consumer<AppointmentId> details;
    CustomersView(DesktopController controller, Consumer<AppointmentId> details) {
        this.controller = controller; this.details = details; show("");
    }
    private void show(String message) {
        var page = Ui.page("Customers", "Manage the people you book for and view their appointment history.");
        var search = new TextField(); search.setId("customers-search"); search.setPromptText("Search names or IDs");
        var table = Ui.<Customer>table("customers-table");
        Ui.column(table, "Customer", 280, Customer::name);
        Ui.column(table, "Reference", 320, c -> c.id().value());
        Runnable filter = () -> table.getItems().setAll(controller.customers().stream().filter(c ->
                (c.name() + " " + c.id().value()).toLowerCase(Locale.ROOT).contains(search.getText().strip().toLowerCase(Locale.ROOT))).toList());
        search.textProperty().addListener((o, a, b) -> filter.run()); filter.run();
        var edit = Ui.action("Edit customer", "customers-edit", () -> edit(table.getSelectionModel().getSelectedItem()));
        var history = Ui.action("View appointments", "customers-appointments", () -> history(table.getSelectionModel().getSelectedItem()));
        edit.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        history.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        var feedback = Ui.feedback("customers-feedback"); feedback.setText(message); feedback.getStyleClass().add("feedback-success");
        page.getChildren().addAll(Ui.actions(Ui.primary("Add customer", "customers-add", () -> edit(null)), edit, history),
                Ui.field("Search customers", search), feedback, table);
        getChildren().setAll(page);
    }
    private void edit(Customer customer) {
        getChildren().setAll(CatalogForms.customer(controller, customer,
                saved -> show("Customer " + saved.name() + (customer == null ? " created" : " updated") + " successfully. Available for booking now."), () -> show("")));
    }
    private void history(Customer customer) {
        var page = Ui.page(customer.name(), "Appointments retain the customer and service details agreed when booked.");
        var table = Ui.<Appointment>table("customer-appointments-table");
        Ui.column(table, "When", 200, a -> controller.format(a.slot().start()));
        Ui.column(table, "Service", 220, a -> a.service().name());
        Ui.column(table, "Provider", 170, a -> a.provider().name()); Ui.statusColumn(table, Appointment::status);
        table.getItems().setAll(controller.customerAppointments(customer.id()));
        var open = Ui.action("View appointment", "customer-appointment-open", () -> details.accept(table.getSelectionModel().getSelectedItem().id()));
        open.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        page.getChildren().addAll(Ui.actions(Ui.action("Back to customers", "customers-back", () -> show("")), open), table);
        getChildren().setAll(page);
    }
}
