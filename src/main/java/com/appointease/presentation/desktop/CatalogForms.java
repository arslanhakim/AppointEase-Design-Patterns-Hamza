package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Detached form values are submitted through the controller, never into domain collections. */
final class CatalogForms {
    private CatalogForms() { }

    static VBox customer(DesktopController controller, Customer old, Consumer<Customer> saved, Runnable cancel) {
        var name = input("customer-name", old == null ? "" : old.name());
        var feedback = Ui.feedback("customer-form-feedback");
        var page = Ui.page(old == null ? "New customer" : "Edit customer", "Customer names are used for booking and appointment history.");
        page.getChildren().addAll(Ui.field("Full name", name), feedback,
                Ui.actions(Ui.primary("Save customer", "customer-save", () -> {
                    try { saved.accept(controller.saveCustomer(old, name.getText())); }
                    catch (RuntimeException ex) { Ui.error(feedback, ex); }
                }), Ui.action("Cancel", "customer-cancel", cancel)));
        return page;
    }

    static VBox service(DesktopController controller, Service old, Consumer<Service> saved, Runnable cancel) {
        var name = input("service-name", old == null ? "" : old.name());
        var duration = input("service-duration", old == null ? "30" : Long.toString(old.duration().toMinutes()));
        var fee = input("service-fee", old == null ? "0.00" : old.baseFee().amount().toPlainString());
        var category = Ui.combo("service-category", List.of(ProviderCategory.values()), DesktopController::title);
        category.setValue(old == null ? ProviderCategory.CONSULTING : old.category());
        var kind = Ui.combo("service-kind", List.of(AppointmentKind.values()), DesktopController::title);
        kind.setValue(old == null ? AppointmentKind.CONSULTATION : old.kind());
        var feedback = Ui.feedback("service-form-feedback");
        var page = Ui.page(old == null ? "New service" : "Edit service",
                "Changes apply to future bookings. Existing appointments retain their agreed service, duration and fee.");
        page.getChildren().addAll(Ui.field("Service name", name),
                Ui.actions(Ui.field("Category", category), Ui.field("Appointment type", kind)),
                Ui.actions(Ui.field("Duration · minutes", duration), Ui.field("Base fee · EUR", fee)), feedback,
                Ui.actions(Ui.primary("Save service", "service-save", () -> {
                    try {
                        var value = new Service(old == null ? new ServiceId(UUID.randomUUID().toString()) : old.id(),
                                name.getText(), Duration.ofMinutes(minutes(duration)), new Money(decimal(fee)), kind.getValue(), category.getValue());
                        saved.accept(controller.saveService(old, value));
                    } catch (RuntimeException ex) { Ui.error(feedback, ex); }
                }), Ui.action("Cancel", "service-cancel", cancel)));
        return page;
    }

    static VBox provider(DesktopController controller, ServiceProvider old, Consumer<ServiceProvider> saved, Runnable cancel) {
        var name = input("provider-name", old == null ? "" : old.name());
        var category = Ui.combo("provider-category", List.of(ProviderCategory.values()), DesktopController::title);
        category.setValue(old == null ? ProviderCategory.CONSULTING : old.category());
        var buffer = input("provider-buffer", old == null ? "15" : Long.toString(old.preparationBuffer().toMinutes()));
        var enabled = new CheckBox("Enabled for new bookings");
        enabled.setId("provider-enabled");
        enabled.setSelected(old == null || old.enabled());
        var services = new LinkedHashMap<ServiceId, CheckBox>();
        var serviceChoices = new FlowPane(12, 8);
        for (var service : controller.services()) {
            var choice = new CheckBox(service.name());
            choice.setId("provider-support-" + service.id().value());
            choice.setSelected(old != null && old.serviceIds().contains(service.id()));
            services.put(service.id(), choice);
            serviceChoices.getChildren().add(choice);
        }
        var modes = new EnumMap<AppointmentMode, CheckBox>(AppointmentMode.class);
        var modeChoices = new FlowPane(12, 8);
        for (var mode : AppointmentMode.values()) {
            var choice = new CheckBox(DesktopController.title(mode));
            choice.setId("provider-support-" + mode.name().toLowerCase(Locale.ROOT));
            choice.setSelected(old == null || old.modes().contains(mode));
            modes.put(mode, choice);
            modeChoices.getChildren().add(choice);
        }
        var hours = new EnumMap<DayOfWeek, TextField>(DayOfWeek.class);
        var week = new GridPane();
        week.setHgap(14); week.setVgap(6);
        int row = 0;
        for (var day : DayOfWeek.values()) {
            String periods = old == null ? (day.getValue() <= 5 ? "09:00-17:00" : "") : old.workingHours()
                    .getOrDefault(day, List.of()).stream().map(p -> p.start() + "-" + p.end()).collect(Collectors.joining(", "));
            var field = input("provider-hours-" + day.name().toLowerCase(Locale.ROOT), periods);
            field.setPromptText("Closed");
            field.setAccessibleText(DesktopController.title(day) + " working periods");
            var label = Ui.label(DesktopController.title(day), "field-label"); label.setLabelFor(field);
            hours.put(day, field);
            week.addRow(row++, label, field);
            GridPane.setHgrow(field, Priority.ALWAYS);
        }
        var feedback = Ui.feedback("provider-form-feedback");
        var page = Ui.page(old == null ? "New provider" : "Edit provider",
                "Active appointments must keep their supported service, mode and reserved working hours. Buffer changes apply to new bookings and reschedules.");
        page.setSpacing(10);
        page.getChildren().addAll(Ui.actions(Ui.field("Provider name", name), Ui.field("Category", category)),
                Ui.text("Supported services · choose at least one", "field-label"), serviceChoices,
                Ui.text("Appointment modes", "field-label"), modeChoices,
                Ui.actions(Ui.field("Preparation · minutes", buffer), enabled),
                Ui.text("Weekly hours · " + controller.time().zone() + "\nUse HH:mm-HH:mm; separate split shifts with commas. Leave closed days blank.", "muted"),
                week, feedback, Ui.actions(Ui.primary("Save provider", "provider-save", () -> {
                    try {
                        var working = new EnumMap<DayOfWeek, List<WorkingPeriod>>(DayOfWeek.class);
                        hours.forEach((day, field) -> working.put(day, periods(field.getText())));
                        var selectedServices = services.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).collect(Collectors.toSet());
                        var selectedModes = modes.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).collect(Collectors.toSet());
                        var value = new ServiceProvider(old == null ? new ProviderId(UUID.randomUUID().toString()) : old.id(), name.getText(),
                                category.getValue(), selectedServices, selectedModes, working, Duration.ofMinutes(minutes(buffer)), enabled.isSelected());
                        saved.accept(controller.saveProvider(old, value));
                    } catch (RuntimeException ex) { Ui.error(feedback, ex); }
                }), Ui.action("Cancel", "provider-cancel", cancel)));
        return page;
    }

    private static TextField input(String id, String value) {
        var input = new TextField(value); input.setId(id); input.setPrefColumnCount(22); return input;
    }
    private static long minutes(TextField input) {
        try { return Long.parseLong(input.getText().strip()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Enter minutes as a whole number."); }
    }
    private static BigDecimal decimal(TextField input) {
        try { return new BigDecimal(input.getText().strip()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Enter a valid EUR amount, for example 25.00."); }
    }
    private static List<WorkingPeriod> periods(String text) {
        if (text.isBlank()) return List.of();
        try {
            return Arrays.stream(text.split(",", -1)).map(part -> {
                var bounds = part.strip().split("-", -1);
                if (bounds.length != 2 || !bounds[0].matches("[0-9]{2}:[0-9]{2}") || !bounds[1].matches("[0-9]{2}:[0-9]{2}"))
                    throw new IllegalArgumentException("Use working periods such as 09:00-12:00, 13:00-17:00.");
                return new WorkingPeriod(LocalTime.parse(bounds[0]), LocalTime.parse(bounds[1]));
            }).toList();
        } catch (DateTimeException ex) { throw new IllegalArgumentException("Enter valid working times between 00:00 and 23:59."); }
    }
}
