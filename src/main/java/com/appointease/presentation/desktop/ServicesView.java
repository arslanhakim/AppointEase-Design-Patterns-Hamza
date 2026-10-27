package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.Locale;
import java.util.stream.Collectors;

final class ServicesView extends VBox {
    private final DesktopController controller;
    ServicesView(DesktopController controller) { this.controller = controller; show(""); }
    private void show(String message) {
        var page = Ui.page("Services", "Maintain your service catalog. Existing bookings keep their agreed details and prices.");
        var search = new TextField(); search.setId("services-search"); search.setPromptText("Search services or categories");
        var table = Ui.<Service>table("services-table");
        Ui.column(table, "Service", 220, Service::name);
        Ui.column(table, "Category", 150, s -> DesktopController.title(s.category()));
        Ui.column(table, "Minutes", 75, s -> Long.toString(s.duration().toMinutes()));
        Ui.column(table, "Base fee", 115, s -> DesktopController.money(s.baseFee()));
        Ui.column(table, "Providers", 200, s -> controller.providers().stream().filter(p -> p.serviceIds().contains(s.id()))
                .map(ServiceProvider::name).collect(Collectors.joining(", ")));
        Runnable filter = () -> table.getItems().setAll(controller.services().stream().filter(s ->
                (s.name() + " " + DesktopController.title(s.category())).toLowerCase(Locale.ROOT).contains(search.getText().strip().toLowerCase(Locale.ROOT))).toList());
        search.textProperty().addListener((o, a, b) -> filter.run()); filter.run();
        var edit = Ui.action("Edit service", "services-edit", () -> edit(table.getSelectionModel().getSelectedItem()));
        edit.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        var provider = Ui.combo("service-provider-select", controller.providers(), ServiceProvider::name);
        provider.getSelectionModel().selectFirst();
        var relationships = Ui.action("Edit supported services & modes", "service-provider-edit", () -> getChildren().setAll(
                CatalogForms.provider(controller, provider.getValue(), saved -> show("Provider relationships updated successfully."), () -> show(""))));
        relationships.disableProperty().bind(provider.valueProperty().isNull());
        var feedback = Ui.feedback("services-feedback"); feedback.setText(message); feedback.getStyleClass().add("feedback-success");
        page.getChildren().addAll(Ui.actions(Ui.primary("Add service", "services-add", () -> edit(null)), edit),
                Ui.field("Search services", search), feedback, table,
                Ui.text("Provider relationships · supported modes are configured per provider", "section-title"),
                Ui.actions(Ui.field("Provider", provider), relationships));
        getChildren().setAll(page);
    }
    private void edit(Service service) {
        getChildren().setAll(CatalogForms.service(controller, service,
                saved -> show("Service " + saved.name() + (service == null ? " created" : " updated") + " successfully. Assign it to a provider to offer bookings."), () -> show("")));
    }
}
