package com.appointease.presentation.desktop;

import com.appointease.DesktopMain;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.robot.Robot;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Opt-in real Windows sidebar checks and raw after captures; separate from the 289 JUnit tests. */
public final class DesktopSidebarCheck {
    private final Stage stage;
    private final boolean capture;
    private final boolean iconFinal;
    private int checks;
    private final AtomicReference<Double> activationOpacity = new AtomicReference<>(1.0);

    private DesktopSidebarCheck(Stage stage, boolean capture, boolean iconFinal) {
        this.stage = stage; this.capture = capture; this.iconFinal = iconFinal;
    }

    public static void main(String[] args) throws Exception {
        Platform.startup(() -> {});
        Platform.setImplicitExit(false);
        Stage stage = fx(() -> { var window = new Stage(); new DesktopMain().start(window); return window; });
        try { new DesktopSidebarCheck(stage, java.util.List.of(args).contains("--capture"),
                java.util.List.of(args).contains("--icon-final")).run(); }
        finally { fx(() -> { stage.close(); return null; }); Platform.exit(); }
    }

    private void run() throws Exception {
        size(false);
        check(() -> {
            require(stage.isShowing(), "Actual packaged JavaFX window opened");
            require(shell().isNavigationExpanded(), "Desktop starts with expanded navigation");
            require(toggle().getText() == null || toggle().getText().isEmpty(), "Toolbar toggle has no Menu text");
            require(toggle().getGraphic().lookup(".navigation-icon-stroke") instanceof SVGPath, "Toggle uses a local vector path");
            require(toggle().getAccessibleText().equals("Collapse sidebar"), "Expanded toggle announces Collapse sidebar");
            toggle().addEventHandler(ActionEvent.ACTION,
                    e -> Platform.runLater(() -> activationOpacity.set(toggle().getGraphic().getOpacity())));
        });
        shot("05-after-desktop-expanded", false);
        var view = fx(() -> scroll().getContent());
        String collapsePath = fx(() -> ((SVGPath) toggle().getGraphic().lookup(".navigation-icon-stroke")).getContent());
        mouseToggle();
        check(() -> {
            require(!shell().isNavigationExpanded() && shell().getLeft() != null, "Pointer collapse retains a navigation rail");
            require(Math.abs(((Region) shell().getLeft()).getWidth() - 64) < 1, "Collapsed rail is 64 logical pixels");
            require(scroll().getContent() == view, "Toggle preserves the exact selected view instance");
            require(!collapsePath.equals(((SVGPath) toggle().getGraphic().lookup(".navigation-icon-stroke")).getContent()), "Expand and collapse glyphs differ");
            require(toggle().getAccessibleText().equals("Expand sidebar") && toggle().getTooltip().getText().equals("Expand sidebar"), "Collapsed toggle label and tooltip match its action");
            boolean reduce = Boolean.parseBoolean(System.getProperty("appointease.reduceMotion", "true"));
            require(reduce ? activationOpacity.get() == 1 : activationOpacity.get() < 1,
                    reduce ? "Reduced motion makes pointer activation immediate" : "Pointer activation uses subtle icon-opacity feedback");
        });
        shot("06-after-desktop-rail", false);
        for (int i = 0; i < 8; i++) {
            pointerClick("navigation-toggle");
            pulse(40);
        }
        pulse(200);
        check(() -> {
            require(!shell().isNavigationExpanded(), "Eight rapid pointer toggles return to the intended rail state");
            require(toggle().getGraphic().getOpacity() == 1, "Interrupted feedback settles fully visible");
            require(scroll().getContent() == view, "Rapid toggles do not recreate page content");
            require(stage.getScene().getFocusOwner() == toggle(), "Toggle retains native keyboard focus");
        });
        // Both native activation keys must work, without pointer animation.
        key(KeyCode.SPACE);
        check(() -> {
            require(shell().isNavigationExpanded(), "Space expands the sidebar");
            require(activationOpacity.get() == 1, "Keyboard activation has no fade");
        });
        key(KeyCode.ENTER);
        check(() -> require(!shell().isNavigationExpanded(), "Enter collapses the sidebar"));
        for (var page : DesktopController.Page.values()) {
            check(() -> button(navId(page)).fire());
            pulse(180);
            check(() -> {
                var button = button(navId(page));
                require(!shell().isNavigationExpanded() && stage.getTitle().equals("AppointEase | " + DesktopController.title(page)), "Rail navigation opens " + page);
                require(button.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY && button.getGraphic() != null
                        && button.getTooltip().getText().equals(DesktopController.title(page))
                        && button.getAccessibleText().contains("current page") && button.getStyleClass().contains("nav-selected"),
                        "Rail " + page + " retains its icon, tooltip, accessible name and selected state");
            });
        }
        // Selection, typed search and an in-progress lifecycle panel survive toggling.
        check(() -> {
            button("nav-appointments").fire();
            ((TextField) node("appointments-search")).setText("seed-ready-001");
            ((TableView<?>) node("appointments-table")).getSelectionModel().selectFirst();
        });
        var table = fx(() -> (TableView<?>) node("appointments-table"));
        var selected = fx(() -> table.getSelectionModel().getSelectedItem());
        toggleTwice();
        check(() -> {
            require(node("appointments-table") == table && table.getSelectionModel().getSelectedItem() == selected
                    && ((TextField) node("appointments-search")).getText().equals("seed-ready-001"), "Table search and selection survive toggles");
            button("appointments-open").fire();
            button("details-complete").fire();
        });
        var accept = fx(() -> node("details-accept"));
        toggleTwice();
        check(() -> require(node("details-accept") == accept, "Open lifecycle confirmation remains intact without committing"));
        size(true);
        check(() -> button("nav-overview").fire());
        shot("07-after-compact-rail", true);
        mouseToggle();
        shot("08-after-compact-expanded", true);
        check(() -> button("nav-overview").fire());
        check(() -> {
            require(!shell().isNavigationExpanded(), "Compact destination selection restores the rail");
            var bounds = button("nav-providers").localToScreen(button("nav-providers").getBoundsInLocal());
            new Robot().mouseMove(bounds.getCenterX(), bounds.getCenterY());
        });
        pulse(650);
        check(() -> require(button("nav-providers").getTooltip().isShowing(), "Hovering the hidden provider label shows its real tooltip"));
        shot("09-after-compact-tooltip", true);
        check(() -> {
            button("nav-providers").getTooltip().hide();
            button("nav-providers").requestFocus();
        });
        key(KeyCode.SPACE);
        check(() -> require(stage.getTitle().equals("AppointEase | Providers"), "Focused rail icon navigates with Space"));
        check(() -> {
            button("nav-new_appointment").fire();
            ((ComboBox<?>) node("booking-customer")).getSelectionModel().selectFirst();
            ((TextField) node("booking-time")).setText("14:45");
            ((CheckBox) node("booking-priority")).setSelected(true);
        });
        var draft = fx(() -> scroll().getContent());
        shot("10-after-compact-draft-rail", true);
        mouseToggle();
        check(() -> require(scroll().getContent() == draft
                && ((TextField) node("booking-time")).getText().equals("14:45")
                && ((CheckBox) node("booking-priority")).isSelected()
                && ((ComboBox<?>) node("booking-customer")).getValue() != null, "Draft customer, time and optional service survive expansion"));
        shot("11-after-compact-draft-expanded", true);
        check(() -> button("booking-review").fire());
        var submit = fx(() -> node("booking-submit"));
        toggleTwice();
        check(() -> require(submit != null && node("booking-submit") == submit, "Reviewed booking stays intact through toggles"));
        check(() -> {
            button("nav-activity").fire();
            scroll().setVvalue(0.55);
        });
        pulse(180);
        double scrollPosition = fx(() -> scroll().getVvalue());
        var activity = fx(() -> scroll().getContent());
        toggleTwice();
        check(() -> require(scroll().getContent() == activity && Math.abs(scroll().getVvalue() - scrollPosition) < 0.01, "Activity content and scroll position survive toggles"));
        for (boolean compact : new boolean[]{false, true}) {
            size(compact);
            for (var page : DesktopController.Page.values()) {
                check(() -> button(navId(page)).fire());
                for (boolean expanded : new boolean[]{true, false}) {
                    check(() -> { if (shell().isNavigationExpanded() != expanded) toggle().fire(); });
                    pulse(200);
                    check(() -> fits(page, compact, expanded));
                }
            }
        }
        System.out.println("SIDEBAR CHECK PASSED: " + checks + " assertions; reducedMotion=" + System.getProperty("appointease.reduceMotion"));
    }

    private void fits(DesktopController.Page page, boolean compact, boolean expanded) {
        var viewport = scroll().lookup(".viewport");
        var visible = viewport.localToScene(viewport.getBoundsInLocal());
        boolean fits = scroll().getContent().getLayoutBounds().getWidth() <= scroll().getViewportBounds().getWidth() + 1;
        for (var node : scroll().getContent().lookupAll(".button")) {
            if (node instanceof Button button && button.isVisible() && button.isManaged()) {
                var bounds = button.localToScene(button.getBoundsInLocal());
                fits &= bounds.getMinX() >= visible.getMinX() - 1 && bounds.getMaxX() <= visible.getMaxX() + 1;
            }
        }
        require(fits, (compact ? "1024x768 " : "1440x900 ") + page + (expanded ? " expanded" : " rail") + " fits content and button widths");
        var rail = shell().getLeft().localToScene(shell().getLeft().getBoundsInLocal());
        require(shell().lookupAll(".nav-button").stream().allMatch(n -> {
            var b = n.localToScene(n.getBoundsInLocal());
            return b.getMinX() >= rail.getMinX() && b.getMaxX() <= rail.getMaxX() && b.getMaxY() <= rail.getMaxY();
        }), "All six navigation controls remain inside the sidebar");
    }

    private void toggleTwice() throws Exception {
        check(() -> toggle().fire()); pulse(180);
        check(() -> toggle().fire()); pulse(180);
    }
    private void mouseToggle() throws Exception { pointerClick("navigation-toggle"); pulse(200); }
    private void pointerClick(String id) throws Exception {
        focusWindow();
        check(() -> {
            var bounds = button(id).localToScreen(button(id).getBoundsInLocal());
            var robot = new Robot();
            robot.mouseMove(bounds.getCenterX(), bounds.getCenterY());
        });
        pulse(60);
        check(() -> new Robot().mousePress(MouseButton.PRIMARY));
        pulse(40);
        check(() -> new Robot().mouseRelease(MouseButton.PRIMARY));
    }
    private void key(KeyCode key) throws Exception {
        focusWindow();
        check(() -> new Robot().keyPress(key)); pulse(40);
        check(() -> new Robot().keyRelease(key)); pulse(200);
    }
    private void focusWindow() throws Exception {
        // Windows may deny programmatic activation after a capture child exits.
        // A real title-bar click activates the window without changing its focused control.
        if (!fx(stage::isFocused)) {
            check(() -> new Robot().mouseMove(stage.getX() + stage.getWidth() / 2, stage.getY() + 14));
            pulse(60);
            check(() -> new Robot().mousePress(MouseButton.PRIMARY)); pulse(40);
            check(() -> new Robot().mouseRelease(MouseButton.PRIMARY)); pulse(180);
        }
        if (!fx(stage::isFocused)) throw new AssertionError("Native input requires the foreground AppointEase window");
    }
    private void size(boolean compact) throws Exception {
        check(() -> {
            stage.setWidth((compact ? 1024 : 1440) / stage.getOutputScaleX());
            stage.setHeight((compact ? 768 : 900) / stage.getOutputScaleY());
            stage.setX(20 / stage.getOutputScaleX()); stage.setY(20 / stage.getOutputScaleY());
            stage.setAlwaysOnTop(true);
            stage.toFront(); stage.requestFocus();
        });
        pulse(250);
    }
    private void shot(String name, boolean compact) throws Exception {
        if (!capture) return;
        if (iconFinal) {
            name = switch (name) {
                case "05-after-desktop-expanded" -> "01-desktop-expanded";
                case "06-after-desktop-rail" -> "02-desktop-collapsed";
                case "07-after-compact-rail" -> "03-compact-collapsed";
                case "08-after-compact-expanded" -> "04-compact-expanded";
                default -> null;
            };
            if (name == null) return;
        }
        pulse(200);
        var process = new ProcessBuilder("powershell", "-NoProfile", "-File", "scripts/capture-ui.ps1", "-View",
                compact ? "Compact" : "Desktop", "-S7BName", name, "-Slice",
                iconFinal ? "sidebar-icon-final" : "sidebar-toggle").inheritIO().start();
        if (!process.waitFor(20, TimeUnit.SECONDS) || process.exitValue() != 0) throw new AssertionError("Capture failed: " + name);
        // Capture releases its native topmost flag; restore the harness window before Robot input.
        check(() -> { stage.setAlwaysOnTop(false); stage.setAlwaysOnTop(true); stage.toFront(); stage.requestFocus(); });
        pulse(180);
    }
    private DesktopShell shell() { return (DesktopShell) stage.getScene().getRoot(); }
    private Node node(String id) { return stage.getScene().lookup("#" + id); }
    private Button button(String id) { return (Button) node(id); }
    private Button toggle() { return button("navigation-toggle"); }
    private ScrollPane scroll() { return (ScrollPane) node("overview-scroll"); }
    private String navId(DesktopController.Page page) { return "nav-" + page.name().toLowerCase(java.util.Locale.ROOT); }
    private void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); checks++; System.out.println("PASS: " + message); }
    private void check(Runnable action) throws Exception { fx(() -> { action.run(); shell().applyCss(); shell().layout(); return null; }); }
    private static void pulse(int millis) throws Exception {
        var done = new CompletableFuture<Void>();
        Platform.runLater(() -> { var pause = new PauseTransition(Duration.millis(millis)); pause.setOnFinished(e -> done.complete(null)); pause.play(); });
        done.get(10, TimeUnit.SECONDS);
    }
    private static <T> T fx(Callable<T> task) throws Exception {
        var done = new CompletableFuture<T>();
        Platform.runLater(() -> { try { done.complete(task.call()); } catch (Throwable e) { done.completeExceptionally(e); } });
        return done.get(10, TimeUnit.SECONDS);
    }
}
