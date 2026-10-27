package com.appointease.presentation.desktop;

import com.appointease.domain.model.AppointmentStatus;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.Locale;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.util.StringConverter;
import java.util.List;
import java.util.function.Function;

/** Small shared native-control vocabulary for the desktop presentation. */
final class Ui {
    private Ui() { }

    static Label label(String text, String style) {
        var label = new Label(text);
        label.getStyleClass().add(style);
        label.setMinWidth(0);
        return label;
    }

    static Label text(String text, String style) {
        var label = label(text, style);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    static Label feedback(String id) {
        var label = text("", "muted");
        label.setId(id);
        label.visibleProperty().bind(label.textProperty().isNotEmpty());
        label.managedProperty().bind(label.visibleProperty());
        return label;
    }

    static DatePicker date(String id, java.time.LocalDate value) {
        var date = new DatePicker(value);
        date.setId(id);
        date.setEditable(false);
        var format = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        date.setConverter(new javafx.util.converter.LocalDateStringConverter(format, format));
        return date;
    }

    static VBox page(String title, String subtitle) {
        var box = new VBox(18, label(title, "page-title"), text(subtitle, "subtitle"));
        box.getStyleClass().add("page");
        box.setMinWidth(0);
        return box;
    }

    static FlowPane actions(Node... nodes) {
        var row = new FlowPane(8, 8, nodes);
        row.setMinWidth(0);
        return row;
    }

    static Button action(String text, String id, Runnable run) {
        var button = button(text, text, run);
        button.setId(id);
        return button;
    }

    static Button primary(String text, String id, Runnable run) {
        var button = action(text, id, run);
        button.getStyleClass().add("primary-button");
        return button;
    }

    static VBox field(String name, Control control) {
        var label = label(name, "field-label");
        label.setLabelFor(control);
        control.setAccessibleText(name);
        control.setMaxWidth(Double.MAX_VALUE);
        var field = new VBox(6, label, control);
        field.setMinWidth(0);
        return field;
    }

    static <T> ComboBox<T> combo(String id, List<T> items, Function<T, String> display) {
        var combo = new ComboBox<T>(FXCollections.observableArrayList(items));
        combo.setId(id);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(T value) { return value == null ? "" : display.apply(value); }
            @Override public T fromString(String value) { throw new UnsupportedOperationException("Selection only"); }
        });
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setMinWidth(0);
        var selected = new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? combo.getPromptText() : display.apply(item));
            }
        };
        combo.setButtonCell(selected);
        combo.promptTextProperty().addListener((o, old, prompt) -> {
            if (combo.getValue() == null) selected.setText(prompt);
        });
        return combo;
    }

    static <T> TableView<T> table(String id) {
        var table = new TableView<T>();
        table.setId(id);
        table.setMinWidth(0);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(text("No matching records. Adjust the filters or choose another date.", "muted"));
        table.setPrefHeight(320);
        table.setFixedCellSize(55);
        return table;
    }

    static <T> void column(TableView<T> table, String title, double width, Function<T, String> value) {
        var column = new TableColumn<T, String>(title);
        column.setCellValueFactory(row -> new ReadOnlyStringWrapper(value.apply(row.getValue())));
        column.setPrefWidth(width);
        column.setMinWidth(65);
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTooltip(empty || item == null ? null : new Tooltip(item));
            }
        });
        table.getColumns().add(column);
    }

    static <T> void statusColumn(TableView<T> table, Function<T, AppointmentStatus> value) {
        var column = new TableColumn<T, AppointmentStatus>("Status");
        column.setCellValueFactory(row -> new javafx.beans.property.ReadOnlyObjectWrapper<>(value.apply(row.getValue())));
        column.setPrefWidth(112);
        column.setMinWidth(100);
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(AppointmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : status(item));
            }
        });
        table.getColumns().add(column);
    }

    static VBox priceSummary(com.appointease.domain.addon.BookingEnhancements quote) {
        var breakdown = new VBox(4, priceLine("Service", DesktopController.money(quote.baseFee())));
        quote.options().additionalFees().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
            var code = entry.getKey();
            breakdown.getChildren().add(priceLine(code.substring(0, 1).toUpperCase(Locale.ROOT) + code.substring(1), DesktopController.money(entry.getValue())));
        });
        var result = new VBox(8, breakdown, priceTotal(quote.totalFee()));
        result.getStyleClass().add("price-summary");
        return result;
    }

    static HBox priceTotal(com.appointease.domain.model.Money amount) {
        var total = new HBox(12, label("Total", "row-primary"), spacer(),
                label(DesktopController.money(amount), "price-total"));
        total.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        total.getStyleClass().add("price-total-row");
        return total;
    }

    private static HBox priceLine(String name, String amount) {
        return new HBox(12, text(name, "muted"), spacer(), label(amount, "row-secondary"));
    }

    static VBox appointmentWhen(java.time.Instant start, java.time.Instant end,
                                String provider, java.time.ZoneId zone) {
        var dateFormat = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);
        var timeFormat = java.time.format.DateTimeFormatter.ofPattern("HH:mm XXX", Locale.ENGLISH);
        var result = new VBox(5, text(dateFormat.format(start.atZone(zone)), "appointment-date"),
                text(timeFormat.format(start.atZone(zone)) + " – " + timeFormat.format(end.atZone(zone)), "appointment-time"),
                text("With " + provider, "appointment-provider"));
        result.getStyleClass().add("appointment-when");
        return result;
    }

    static void error(Label label, RuntimeException error) {
        String message = error instanceof com.appointease.domain.scheduling.SlotUnavailableException
                ? "This time conflicts with an existing reservation. Choose another time. No changes were saved."
                : error.getMessage();
        label.setText(message == null ? "Unable to complete this request. Refresh and try again." : message);
        label.getStyleClass().setAll("feedback-error");
        label.setAccessibleText("Error: " + label.getText());
    }

    static Button button(String text, String accessible, Runnable action) {
        var button = new Button(text);
        button.setAccessibleText(accessible);
        button.setTooltip(new Tooltip(accessible));
        button.setOnAction(event -> action.run());
        return button;
    }

    static Region spacer() {
        var space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        return space;
    }

    static Label status(AppointmentStatus status) {
        String name = status.name().toLowerCase(Locale.ROOT);
        var label = label(name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1), "status");
        label.getStyleClass().add("status-" + name);
        label.setAccessibleText("Appointment status: " + label.getText());
        return label;
    }
}
