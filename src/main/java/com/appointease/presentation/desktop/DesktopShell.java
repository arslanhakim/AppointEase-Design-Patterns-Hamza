package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import java.time.LocalDate;
import java.util.EnumMap;

/** Six working destinations sharing one injected presentation controller. */
public final class DesktopShell extends BorderPane {
    private final DesktopController controller;
    private final OverviewView overview;
    private final LocalDate initialDay;
    private final VBox sidebar;
    private final ScrollPane scroll = new ScrollPane();
    private final Label breadcrumb = Ui.label("Workspace / Overview", "breadcrumb");
    private final ReadOnlyStringWrapper pageTitle = new ReadOnlyStringWrapper("Overview");
    private final EnumMap<DesktopController.Page, Button> navigation = new EnumMap<>(DesktopController.Page.class);
    private final Button sidebarToggle = new Button();
    private final NavigationIcon toggleIcon = NavigationIcon.sidebar();
    private final FadeTransition toggleFeedback = new FadeTransition(Duration.millis(140), toggleIcon);
    private final boolean reducedMotion = Boolean.parseBoolean(System.getProperty("appointease.reduceMotion", "true"));
    private final VBox wordmark = new VBox(5, Ui.label("AppointEase", "brand"), Ui.label("APPOINTMENT MANAGEMENT", "brand-caption"));
    private final Label monogram = Ui.label("Ae", "rail-brand");
    private final VBox footer = new VBox(9);
    private BookingView booking;
    private ScheduleView schedule;
    private double scheduleScroll;
    private boolean compact;
    private boolean expanded = true;
    private boolean pointerToggle;

    public DesktopShell(DesktopController controller, OverviewView overview, LocalDate day) {
        this.controller = controller;
        this.overview = overview;
        this.initialDay = day;
        breadcrumb.textProperty().bind(pageTitle.concat("").map(title -> "Workspace / " + title));
        getStyleClass().add("app-shell");
        sidebar = sidebar();
        setLeft(sidebar);
        sidebarToggle.setId("navigation-toggle");
        sidebarToggle.setGraphic(toggleIcon);
        sidebarToggle.getStyleClass().add("sidebar-toggle");
        sidebarToggle.setMinSize(36, 36);
        sidebarToggle.setPrefSize(36, 36);
        sidebarToggle.setMaxSize(36, 36);
        sidebarToggle.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> pointerToggle = e.getButton() == MouseButton.PRIMARY && !e.isSynthesized());
        sidebarToggle.addEventFilter(KeyEvent.KEY_PRESSED, e -> pointerToggle = false);
        sidebarToggle.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> Platform.runLater(() -> pointerToggle = false));
        sidebarToggle.setOnAction(e -> { setExpanded(!expanded, pointerToggle); pointerToggle = false; });
        toggleFeedback.setInterpolator(Interpolator.SPLINE(0.23, 1, 0.32, 1));
        renderNavigation();
        var badge = Ui.label("LOCAL WORKSPACE", "workspace-badge");
        var header = new HBox(12, sidebarToggle, breadcrumb, Ui.spacer(), badge);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("topbar");
        scroll.setId("overview-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("workspace-scroll");
        var workspace = new BorderPane(scroll);
        workspace.setMinWidth(0);
        workspace.setTop(header);
        workspace.widthProperty().addListener((o, old, width) -> {
            badge.setVisible(width.doubleValue() >= 750);
            badge.setManaged(width.doubleValue() >= 750);
        });
        setCenter(workspace);
        overview.setActions(() -> newAppointment(null), () -> navigate(DesktopController.Page.APPOINTMENTS),
                () -> navigate(DesktopController.Page.SCHEDULE), this::details);
        widthProperty().addListener((obs, old, width) -> {
            boolean small = width.doubleValue() < 1000;
            if (small != compact) { compact = small; setExpanded(!small, false); }
            if (scroll.getContent() != null && scroll.getContent().getStyleClass().contains("page")) {
                scroll.getContent().getStyleClass().remove("compact");
                if (compact) scroll.getContent().getStyleClass().add("compact");
            }
        });
        navigate(DesktopController.Page.OVERVIEW);
    }

    public void toggleNavigation() { setExpanded(!expanded, false); }

    public boolean isNavigationExpanded() { return expanded; }

    private void setExpanded(boolean expanded, boolean pointer) {
        if (this.expanded == expanded) return;
        this.expanded = expanded;
        boolean interrupted = toggleFeedback.getStatus() == javafx.animation.Animation.Status.RUNNING;
        double opacity = toggleIcon.getOpacity();
        toggleFeedback.stop();
        renderNavigation();
        // A single layout update keeps forms stable. Only the changed glyph fades; keyboard is immediate.
        if (pointer && !reducedMotion) {
            toggleIcon.setOpacity(interrupted ? opacity : 0.65);
            toggleFeedback.setFromValue(toggleIcon.getOpacity());
            toggleFeedback.setToValue(1);
            toggleFeedback.playFromStart();
        } else toggleIcon.setOpacity(1);
    }

    private void renderNavigation() {
        sidebar.getStyleClass().remove("sidebar-rail");
        if (!expanded) sidebar.getStyleClass().add("sidebar-rail");
        sidebar.setMinWidth(expanded ? 214 : 64);
        sidebar.setPrefWidth(expanded ? 214 : 64);
        sidebar.setMaxWidth(expanded ? 214 : 64);
        wordmark.setVisible(expanded);
        wordmark.setManaged(expanded);
        monogram.setVisible(!expanded);
        monogram.setManaged(!expanded);
        footer.setVisible(expanded);
        footer.setManaged(expanded);
        navigation.forEach((page, button) -> {
            button.setContentDisplay(expanded ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY);
            button.setAlignment(expanded ? Pos.CENTER_LEFT : Pos.CENTER);
        });
        toggleIcon.expanded(expanded);
        String action = expanded ? "Collapse sidebar" : "Expand sidebar";
        sidebarToggle.setAccessibleText(action);
        sidebarToggle.setAccessibleHelp(expanded ? "Navigation labels are visible" : "Navigation icons remain available");
        var tooltip = new Tooltip(action);
        tooltip.getStyleClass().add("navigation-tooltip");
        sidebarToggle.setTooltip(tooltip);
    }

    public ReadOnlyStringProperty pageTitleProperty() { return pageTitle.getReadOnlyProperty(); }

    public void navigate(DesktopController.Page page) {
        rememberSchedule();
        controller.navigate(page);
        navigation.forEach((key, button) -> {
            button.getStyleClass().remove("nav-selected");
            if (key == page) button.getStyleClass().add("nav-selected");
            button.setAccessibleText(DesktopController.title(key) + (key == page ? ", current page" : ""));
        });
        pageTitle.set(DesktopController.title(page));
        if (compact) setExpanded(false, false);
        try {
            Node view = switch (page) {
                case OVERVIEW -> { overview.refresh(); yield overview; }
                case APPOINTMENTS -> new AppointmentsView(controller, this::details, () -> navigate(DesktopController.Page.SCHEDULE), () -> newAppointment(null));
                case NEW_APPOINTMENT -> {
                    if (booking == null || booking.completed()) booking = new BookingView(controller, overview.selectedDate(), null, this::details);
                    booking.refreshCatalogs();
                    yield booking;
                }
                case PROVIDERS -> new ProvidersView(controller, initialDay, this::newAppointment);
                case CUSTOMERS -> new CustomersView(controller, this::details);
                case SERVICES -> new ServicesView(controller);
                case SCHEDULE -> {
                    if (schedule == null) schedule = new ScheduleView(controller, overview.selectedDate(), this::details,
                            () -> navigate(DesktopController.Page.APPOINTMENTS));
                    else schedule.reload();
                    yield schedule;
                }
                case ACTIVITY -> new ActivityView(controller);
            };
            show(view);
            if (view == schedule) Platform.runLater(() -> {
                if (scroll.getContent() != schedule) return;
                applyCss(); layout();
                schedule.restoreFocus();
                scroll.setVvalue(scheduleScroll);
            });
        } catch (RuntimeException ex) {
            var message = Ui.text("", "feedback-error");
            Ui.error(message, ex);
            var error = Ui.page("Unable to load view", "Your saved appointments remain in this session.");
            error.getChildren().addAll(message, Ui.action("Try again", "view-retry", () -> navigate(page)));
            show(error);
        }
    }

    private void newAppointment(ServiceProvider provider) {
        booking = new BookingView(controller, overview.selectedDate(), provider, this::details);
        navigate(DesktopController.Page.NEW_APPOINTMENT);
    }
    private void details(AppointmentId id) {
        boolean fromSchedule = scroll.getContent() == schedule && schedule != null;
        navigate(DesktopController.Page.APPOINTMENTS);
        pageTitle.set("Appointment details");
        show(new AppointmentDetailsView(controller, id, () -> navigate(fromSchedule
                ? DesktopController.Page.SCHEDULE : DesktopController.Page.APPOINTMENTS), fromSchedule));
    }
    private void rememberSchedule() {
        if (schedule != null && scroll.getContent() == schedule) {
            scheduleScroll = scroll.getVvalue();
            schedule.rememberFocus();
        }
    }
    private void show(Node node) {
        node.getStyleClass().remove("compact");
        if (compact && node.getStyleClass().contains("page")) node.getStyleClass().add("compact");
        scroll.setFitToHeight(node instanceof AppointmentsView || node instanceof ActivityView);
        scroll.setContent(node);
        scroll.setVvalue(0);
    }

    private VBox sidebar() {
        var brand = new StackPane(wordmark, monogram);
        brand.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(monogram, Pos.TOP_CENTER);
        brand.setMinHeight(74);
        brand.setPrefHeight(74);
        brand.getStyleClass().add("brand-block");
        wordmark.setMinWidth(0);
        var links = new VBox(5);
        for (var page : DesktopController.Page.values()) {
            var button = Ui.action(DesktopController.title(page), "nav-" + page.name().toLowerCase(java.util.Locale.ROOT), () -> navigate(page));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMinWidth(0);
            button.setMinHeight(42);
            button.setPrefHeight(42);
            button.setMaxHeight(42);
            button.setGraphic(NavigationIcon.page(page));
            button.setGraphicTextGap(10);
            button.getTooltip().setShowDelay(Duration.millis(350));
            button.getTooltip().getStyleClass().add("navigation-tooltip");
            button.setAlignment(Pos.CENTER_LEFT);
            button.getStyleClass().add("nav-button");
            navigation.put(page, button);
            links.getChildren().add(button);
        }
        var space = new VBox();
        VBox.setVgrow(space, Priority.ALWAYS);
        footer.getChildren().setAll(Ui.text("LOCAL WORKSPACE", "sidebar-caption"),
                Ui.text(controller.time().zone().getId(), "sidebar-note"),
                Ui.text("Session data.\nResets when you close the app.", "sidebar-note"));
        footer.getStyleClass().add("sidebar-footer");
        var result = new VBox(16, brand, links, space, footer);
        result.setId("navigation-sidebar");
        result.setPrefWidth(214);
        result.setMinWidth(214);
        result.getStyleClass().add("sidebar");
        return result;
    }
}
