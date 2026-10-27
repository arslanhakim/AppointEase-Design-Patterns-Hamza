package com.appointease.presentation.desktop;

import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.function.Consumer;

final class AppointmentsView extends VBox {
    private final TableView<Appointment> table = Ui.table("appointments-table");
    AppointmentsView(DesktopController controller, Consumer<AppointmentId> details, Runnable schedule, Runnable create) {
        getStyleClass().add("page");
        setSpacing(12);
        var search = new TextField();
        search.setPromptText("Customer, provider, service or reference");
        search.setId("appointments-search");
        search.setPrefColumnCount(26);
        var status = Ui.combo("appointments-status", List.of(AppointmentStatus.values()), DesktopController::title);
        status.setPromptText("All statuses");
        var provider = Ui.combo("appointments-provider", controller.providers(), ServiceProvider::name);
        provider.setPromptText("All providers");
        var count = Ui.text("", "muted");
        Runnable refresh = () -> {
            table.getItems().setAll(controller.appointments(search.getText(), status.getValue(),
                    provider.getValue() == null ? null : provider.getValue().id(), null));
            count.setText(table.getItems().size() + " appointments · All dates, including historical and cancelled bookings");
        };
        Ui.column(table, "Date & time", 165, a -> controller.format(a.slot().start()).replace(", ", "\n"));
        Ui.column(table, "Customer / service", 220, a -> a.customer().name() + "\n" + a.service().name());
        Ui.column(table, "Provider", 165, a -> a.provider().name());
        Ui.statusColumn(table, Appointment::status);
        var open = Ui.primary("View appointment", "appointments-open", () -> details.accept(table.getSelectionModel().getSelectedItem().id()));
        open.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        table.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !open.isDisabled()) open.fire(); });
        table.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER && !open.isDisabled()) { open.fire(); e.consume(); } });
        search.textProperty().addListener((o, a, b) -> refresh.run());
        status.valueProperty().addListener((o, a, b) -> refresh.run());
        provider.valueProperty().addListener((o, a, b) -> refresh.run());
        getChildren().setAll(Ui.label("Appointments", "page-title"),
                Ui.text("Browse the full booking history and manage each appointment.", "subtitle"),
                Ui.actions(Ui.primary("New Appointment", "appointments-new", create), Ui.action("Schedule view", "appointments-schedule", schedule)),
                Ui.actions(Ui.field("Search", search), Ui.field("Status", status), Ui.field("Provider", provider),
                        Ui.action("Clear filters", "appointments-clear", () -> { search.clear(); status.setValue(null); provider.setValue(null); })),
                Ui.actions(open, Ui.action("Refresh", "appointments-refresh", refresh)), count, table);
        refresh.run();
    }
}
