package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

final class ProvidersView extends VBox {
    ProvidersView(DesktopController controller, LocalDate day, Consumer<ServiceProvider> book) {
        getStyleClass().add("page");
        setSpacing(16);
        var provider = Ui.combo("provider-select", controller.providers(), p -> p.name() + " · " + DesktopController.title(p.category()));
        var detail = new VBox(14);
        Runnable render = () -> {
            var p = provider.getValue();
            if (p == null) return;
            var service = Ui.combo("provider-service", controller.services(p), s -> s.name() + " · "
                    + s.duration().toMinutes() + " min · " + DesktopController.money(s.baseFee()));
            service.getSelectionModel().selectFirst();
            var mode = Ui.combo("provider-mode", p.modes().stream().sorted().toList(), DesktopController::title);
            mode.getSelectionModel().selectFirst();
            var date = Ui.date("provider-date", day);
            var time = new TextField("14:00");
            time.setId("provider-time");
            time.setPrefColumnCount(7);
            var buffer = new CheckBox("Include preparation buffer");
            var feedback = Ui.text("Select a service, date and time to check local availability.", "muted");
            feedback.setId("provider-feedback");
            String hours = p.workingHours().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
                    .map(e -> DesktopController.title(e.getKey()) + "  " + e.getValue().stream()
                            .map(period -> period.start() + "–" + period.end()).collect(Collectors.joining(", ")))
                    .collect(Collectors.joining("   ·   "));
            detail.getChildren().setAll(Ui.label(p.name(), "section-title"),
                    Ui.text("Working hours · " + controller.time().zone() + "\n" + hours
                            + "\nPreparation buffer: " + p.preparationBuffer().toMinutes() + " minutes", "row-secondary"),
                    Ui.field("Supported services & base fees", service),
                    Ui.actions(Ui.field("Mode", mode), Ui.field("Date", date), Ui.field("Time · HH:mm", time)), buffer,
                    Ui.actions(Ui.action("Check availability", "provider-check", () -> {
                        try {
                            controller.prepare(new DesktopController.Draft(controller.customers().getFirst(), p,
                                    service.getValue(), date.getValue(), time.getText(), mode.getValue(), false, false, buffer.isSelected()));
                            feedback.getStyleClass().setAll("feedback-success");
                            feedback.setText("Available at " + controller.format(controller.resolve(date.getValue(), time.getText()).toInstant())
                                    + ". Advisory only; this time has not been reserved.");
                        } catch (RuntimeException ex) { Ui.error(feedback, ex); }
                    }), Ui.primary("New appointment with provider", "provider-book", () -> book.accept(p))), feedback);
        };
        provider.valueProperty().addListener((o, a, b) -> render.run());
        detail.getStyleClass().add("detail-panel");
        getChildren().setAll(Ui.label("Providers", "page-title"),
                Ui.text("Find the right service, inspect working hours and check an exact time.", "subtitle"),
                Ui.field("Browse providers", provider), detail);
        provider.getSelectionModel().selectFirst();
    }
}
