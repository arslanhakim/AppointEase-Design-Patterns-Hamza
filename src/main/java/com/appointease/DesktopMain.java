package com.appointease;

import com.appointease.infrastructure.seed.DesktopDemoSeed;
import com.appointease.presentation.desktop.DesktopShell;
import com.appointease.presentation.desktop.OverviewModel;
import com.appointease.presentation.desktop.OverviewView;
import com.appointease.presentation.desktop.DesktopController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Objects;

/** JavaFX lifecycle/composition entry point; Main owns the shared booking-service wiring. */
public final class DesktopMain extends Application {
    @Override public void start(Stage stage) {
        var zone = ZoneId.of(System.getProperty("appointease.zone", "Europe/Vilnius"));
        var context = Main.createContext(Clock.systemUTC(), zone);
        var day = DesktopDemoSeed.populate(context.facade(), context.time());
        var overview = new OverviewView(new OverviewModel(context.facade(), context.customers(), context.time()), day);
        var controller = new DesktopController(context.facade(), context.customers(), context.time(), context.notifications(), context.audit());
        var shell = new DesktopShell(controller, overview, day);
        var scene = new Scene(shell, 1424, 861);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/appointease/presentation/desktop/theme.css")).toExternalForm());
        stage.titleProperty().bind(javafx.beans.binding.Bindings.concat("AppointEase | ", shell.pageTitleProperty()));
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(580);
        stage.setWidth(1440);
        stage.setHeight(900);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
