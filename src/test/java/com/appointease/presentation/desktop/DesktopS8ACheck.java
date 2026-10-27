package com.appointease.presentation.desktop;

import com.appointease.DesktopMain;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;
import java.util.concurrent.*;

/** Opt-in packaged Windows form interactions and unmodified native window captures. */
public final class DesktopS8ACheck {
    private final Stage stage;
    private final boolean capture;
    private int checks;
    private Customer customer;
    private Service service;
    private ServiceProvider provider;
    private Appointment booked;
    private DesktopS8ACheck(Stage stage, boolean capture) { this.stage = stage; this.capture = capture; }
    public static void main(String[] args) throws Exception {
        Platform.startup(() -> { }); Platform.setImplicitExit(false);
        var stage = fx(() -> { var s = new Stage(); new DesktopMain().start(s); return s; });
        try { new DesktopS8ACheck(stage, List.of(args).contains("--capture")).run(); }
        finally { fx(() -> { stage.close(); return null; }); Platform.exit(); }
    }
    private void run() throws Exception {
        size(false);
        check(() -> { click("nav-customers"); require(table("customers-table").getItems().size() == 3, "Seed customers are listed"); });
        shot("01-customers-list", false);
        check(() -> click("customers-add"));
        shot("02-new-customer-form", false);
        size(true); shot("16-new-customer-compact", true); size(false);
        check(() -> {
            click("customer-save"); require(label("customer-form-feedback").getText().contains("blank"), "Blank customer rejected in form");
            text("customer-name").setText("Nadia Rahman"); click("customer-save");
            require(label("customers-feedback").getText().contains("created successfully"), "Customer success feedback");
            text("customers-search").setText("nadia");
            require(table("customers-table").getItems().size() == 1, "Case-insensitive customer search");
            customer = (Customer) table("customers-table").getItems().getFirst();
        });
        shot("03-customer-created", false);
        check(() -> {
            table("customers-table").getSelectionModel().selectFirst(); click("customers-edit");
            text("customer-name").setText("Nadia Rahman Ali"); click("customer-save");
            text("customers-search").setText("Nadia"); customer = (Customer) table("customers-table").getItems().getFirst();
            require(customer.name().equals("Nadia Rahman Ali"), "Customer edit saved with same selectable record");
            table("customers-table").getSelectionModel().selectFirst(); click("customers-appointments");
            require(table("customer-appointments-table").getItems().isEmpty(), "New customer history is empty");
            click("nav-services");
        });
        shot("06-services-management", false);
        check(() -> click("services-add"));
        shot("07-new-service-form", false);
        check(() -> {
            text("service-name").setText("Planning consultation"); text("service-fee").setText("-0.001"); click("service-save");
            require(label("service-form-feedback").getText().contains("negative"), "Negative price rejected before rounding in form");
            text("service-duration").setText("0"); text("service-fee").setText("35.125"); click("service-save");
            require(label("service-form-feedback").getText().contains("positive"), "Zero duration rejected in form");
            text("service-duration").setText("45"); click("service-save");
            text("services-search").setText("Planning"); service = (Service) table("services-table").getItems().getFirst();
            require(service.baseFee().amount().toPlainString().equals("35.13"), "Service price is EUR scale two HALF_UP");
            table("services-table").getSelectionModel().selectFirst(); click("services-edit");
            text("service-duration").setText("60"); click("service-save"); text("services-search").setText("Planning");
            service = (Service) table("services-table").getItems().getFirst(); require(service.duration().toMinutes() == 60, "Service edit updates duration");
            click("nav-providers");
        });
        shot("04-providers-management", false);
        check(() -> click("providers-add"));
        shot("05-new-provider-form", false);
        size(true); shot("10-new-provider-compact", true);
        check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(1));
        shot("15-provider-hours-compact", true);
        check(() -> ((ScrollPane) node("overview-scroll")).setVvalue(0)); size(false);
        check(() -> {
            text("provider-name").setText("Avery Quinn"); click("provider-save");
            require(label("provider-form-feedback").getText().contains("supported services"), "Provider requires a supported service");
            box("provider-support-" + service.id().value()).setSelected(true);
            text("provider-hours-monday").setText("09:00-12:00, 11:00-17:00"); click("provider-save");
            require(label("provider-form-feedback").getText().contains("overlap"), "Overlapping working periods rejected");
            text("provider-hours-monday").setText("09:00-12:00, 13:00-17:00");
            text("provider-buffer").setText("20"); click("provider-save");
            require(label("providers-feedback").getText().contains("created successfully"), "Provider created through form");
            provider = (ServiceProvider) combo("provider-select").getValue();
            require(provider.preparationBuffer().toMinutes() == 20 && provider.serviceIds().contains(service.id()), "Provider service and buffer saved");
            click("providers-edit"); text("provider-name").setText("Avery Quinn PhD"); click("provider-save");
            provider = (ServiceProvider) combo("provider-select").getValue(); require(provider.name().endsWith("PhD"), "Provider edit saved");
            click("nav-new_appointment"); combo("booking-customer").setValue(customer);
            combo("booking-provider").setValue(provider); text("booking-time").setText("14:00");
            combo("booking-mode").setValue(AppointmentMode.ONLINE);
            box("booking-priority").setSelected(true); box("booking-reminder").setSelected(true); box("booking-buffered").setSelected(true);
        });
        shot("08-booking-new-customer", false);
        check(() -> {
            click("booking-new-customer"); text("customer-name").setText("Cancel me"); click("customer-cancel");
            require(combo("booking-customer").getValue().equals(customer), "Cancelling inline creation preserves customer");
            click("booking-new-customer"); text("customer-name").setText("Morgan Rivera"); click("customer-save");
            customer = (Customer) combo("booking-customer").getValue();
            require(customer.name().equals("Morgan Rivera"), "Inline customer is immediately selected");
            require(combo("booking-provider").getValue().equals(provider) && combo("booking-service").getValue().equals(service), "Inline creation preserves provider and service");
            require(text("booking-time").getText().equals("14:00") && combo("booking-mode").getValue() == AppointmentMode.ONLINE
                    && box("booking-priority").isSelected() && box("booking-reminder").isSelected() && box("booking-buffered").isSelected(), "Inline creation preserves time mode and options");
            require(label("booking-feedback").getText().contains("selected"), "Inline success feedback visible");
        });
        shot("09-inline-customer-created", false);
        pulse();
        check(() -> {
            var visible = node("overview-scroll").lookup(".viewport");
            var viewport = visible.localToScene(visible.getBoundsInLocal());
            var feedback = label("booking-feedback").localToScene(label("booking-feedback").getBoundsInLocal());
            require(feedback.getMinY() >= viewport.getMinY() && feedback.getMaxY() <= viewport.getMaxY(), "Inline creation success is visible without scrolling");
        });
        size(true); shot("11-booking-compact", true); size(false);
        check(() -> {
            click("booking-review"); click("booking-submit"); require(node("booking-receipt") != null, "Booking newly created customer provider and service succeeds");
            click("nav-customers"); text("customers-search").setText(customer.id().value()); table("customers-table").getSelectionModel().selectFirst();
            click("customers-appointments"); require(table("customer-appointments-table").getItems().size() == 1, "Customer appointment history includes new booking");
            booked = (Appointment) table("customer-appointments-table").getItems().getFirst();
            require(booked.totalFee().amount().toPlainString().equals("47.13"), "New booking includes catalog price and existing decorators");
            click("nav-providers"); combo("provider-select").setValue(provider); click("providers-edit");
            box("provider-enabled").setSelected(false); click("provider-save");
            require(label("provider-form-feedback").getText().contains("active appointment"), "Disable cannot invalidate active booking");
            box("provider-enabled").setSelected(true); box("provider-support-online").setSelected(false); click("provider-save");
            require(label("provider-form-feedback").getText().contains("active appointment"), "Mode removal cannot invalidate active booking");
            click("provider-cancel"); click("nav-services"); text("services-search").setText("Planning");
            table("services-table").getSelectionModel().selectFirst(); click("services-edit");
            text("service-name").setText("Planning follow-up"); text("service-fee").setText("80.00"); text("service-duration").setText("30"); click("service-save");
            combo("service-provider-select").setValue(provider); click("service-provider-edit");
            require(box("provider-support-" + service.id().value()).isSelected(), "Service page opens actual provider relationships"); click("provider-cancel");
            click("nav-customers"); text("customers-search").setText(customer.id().value()); table("customers-table").getSelectionModel().selectFirst(); click("customers-appointments");
            var after = (Appointment) table("customer-appointments-table").getItems().getFirst();
            require(after == booked && after.service().duration().toMinutes() == 60 && after.service().name().equals("Planning consultation")
                    && after.totalFee().amount().toPlainString().equals("47.13"), "Catalog edit retains agreed appointment details duration and fee");
        });
        size(true);
        check(() -> click("nav-customers")); shot("12-customers-compact", true);
        check(() -> click("nav-services")); shot("13-services-compact", true);
        check(() -> click("services-add")); shot("14-new-service-compact", true);
        check(() -> click("service-cancel"));
        for (boolean compact : List.of(false, true)) {
            size(compact);
            for (String page : List.of("customers", "providers", "services")) {
                check(() -> click("nav-" + page)); pulse();
                check(() -> {
                    var scroll = (ScrollPane) node("overview-scroll");
                    require(scroll.getContent().getLayoutBounds().getWidth() <= scroll.getViewportBounds().getWidth() + 1,
                            page + " content fits " + (compact ? "compact" : "desktop") + " viewport");
                    var bounds = scroll.lookup(".viewport").localToScene(scroll.lookup(".viewport").getBoundsInLocal());
                    require(scroll.getContent().lookupAll(".button").stream().filter(Node::isVisible).allMatch(n -> {
                        var b = n.localToScene(n.getBoundsInLocal()); return b.getMinX() >= bounds.getMinX() - 1 && b.getMaxX() <= bounds.getMaxX() + 1;
                    }), page + " actions fit viewport");
                });
            }
        }
        System.out.println("S8A CHECK PASSED: " + checks + " assertions");
    }
    private void size(boolean compact) throws Exception {
        check(() -> { stage.setWidth((compact ? 1024 : 1440) / stage.getOutputScaleX()); stage.setHeight((compact ? 768 : 900) / stage.getOutputScaleY());
            stage.setX(20 / stage.getOutputScaleX()); stage.setY(20 / stage.getOutputScaleY()); stage.toFront(); }); pulse();
    }
    private void shot(String name, boolean compact) throws Exception {
        if (!capture) return;
        pulse();
        var process = new ProcessBuilder("powershell", "-NoProfile", "-File", "scripts/capture-ui.ps1", "-View", compact ? "Compact" : "Desktop",
                "-S7BName", name, "-Slice", "S8A").inheritIO().start();
        if (!process.waitFor(20, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new AssertionError("Capture timed out"); }
        if (process.exitValue() != 0) throw new AssertionError("Capture failed: " + name); pulse();
    }
    private Node node(String id) { return stage.getScene().lookup("#" + id); }
    private TextField text(String id) { return (TextField) node(id); }
    private Label label(String id) { return (Label) node(id); }
    private CheckBox box(String id) { return (CheckBox) node(id); }
    @SuppressWarnings("unchecked") private ComboBox<Object> combo(String id) { return (ComboBox<Object>) node(id); }
    @SuppressWarnings("unchecked") private TableView<Object> table(String id) { return (TableView<Object>) node(id); }
    private void click(String id) { var button = (Button) node(id); if (button == null || button.isDisabled()) throw new AssertionError("Unavailable control: " + id); button.fire(); }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; System.out.println("PASS: " + message); }
    private void check(Runnable action) throws Exception { fx(() -> { action.run(); stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout(); return null; }); }
    private static void pulse() throws Exception {
        var result = new CompletableFuture<Void>(); Platform.runLater(() -> { var pause = new PauseTransition(Duration.millis(250)); pause.setOnFinished(e -> result.complete(null)); pause.play(); }); result.get(10, TimeUnit.SECONDS);
    }
    private static <T> T fx(Callable<T> action) throws Exception {
        var result = new CompletableFuture<T>(); Platform.runLater(() -> { try { result.complete(action.call()); } catch (Throwable ex) { result.completeExceptionally(ex); } }); return result.get(15, TimeUnit.SECONDS);
    }
}
