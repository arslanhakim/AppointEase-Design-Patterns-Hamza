package com.appointease.presentation.desktop;

import com.appointease.domain.model.*;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private BookingView booking;
    private boolean compact;

    public DesktopShell(DesktopController controller, OverviewView overview, LocalDate day) {
        this.controller = controller;
        this.overview = overview;
        this.initialDay = day;
        breadcrumb.textProperty().bind(pageTitle.concat("").map(title -> "Workspace / " + title));
        getStyleClass().add("app-shell");
        sidebar = sidebar();
        setLeft(sidebar);
        var menu = Ui.action("Menu", "navigation-toggle", this::toggleNavigation);
        menu.getStyleClass().add("quiet-button");
        var badge = Ui.label("DEMO WORKSPACE", "workspace-badge");
        var header = new HBox(14, menu, breadcrumb, Ui.spacer(), badge);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("topbar");
        scroll.setId("overview-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("workspace-scroll");
        var workspace = new BorderPane(scroll);
        workspace.setMinWidth(0);
        workspace.setTop(header);
        setCenter(workspace);
        overview.setActions(() -> newAppointment(null), () -> navigate(DesktopController.Page.APPOINTMENTS),
                () -> navigate(DesktopController.Page.SCHEDULE), this::details);
        widthProperty().addListener((obs, old, width) -> {
            boolean small = width.doubleValue() < 1000;
            if (small != compact) { compact = small; setLeft(small ? null : sidebar); }
            if (scroll.getContent() != null && scroll.getContent().getStyleClass().contains("page")) {
                scroll.getContent().getStyleClass().remove("compact");
                if (compact) scroll.getContent().getStyleClass().add("compact");
            }
            badge.setVisible(width.doubleValue() >= 750);
            badge.setManaged(width.doubleValue() >= 750);
        });
        navigate(DesktopController.Page.OVERVIEW);
    }

    public void toggleNavigation() { setLeft(getLeft() == null ? sidebar : null); }

    public ReadOnlyStringProperty pageTitleProperty() { return pageTitle.getReadOnlyProperty(); }

    public void navigate(DesktopController.Page page) {
        controller.navigate(page);
        navigation.forEach((key, button) -> {
            button.getStyleClass().remove("nav-selected");
            if (key == page) button.getStyleClass().add("nav-selected");
            button.setAccessibleText(DesktopController.title(key) + (key == page ? ", current page" : ""));
        });
        pageTitle.set(DesktopController.title(page));
        if (compact) setLeft(null);
        try {
            Node view = switch (page) {
                case OVERVIEW -> { overview.refresh(); yield overview; }
                case APPOINTMENTS -> new AppointmentsView(controller, this::details, () -> navigate(DesktopController.Page.SCHEDULE), () -> newAppointment(null));
                case NEW_APPOINTMENT -> {
                    if (booking == null || booking.completed()) booking = new BookingView(controller, overview.selectedDate(), null, this::details);
                    yield booking;
                }
                case PROVIDERS -> new ProvidersView(controller, initialDay, this::newAppointment);
                case SCHEDULE -> new ScheduleView(controller, overview.selectedDate(), this::details, () -> navigate(DesktopController.Page.APPOINTMENTS));
                case ACTIVITY -> new ActivityView(controller);
            };
            show(view);
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
        navigate(DesktopController.Page.APPOINTMENTS);
        pageTitle.set("Appointment details");
        show(new AppointmentDetailsView(controller, id, () -> navigate(DesktopController.Page.APPOINTMENTS)));
    }
    private void show(Node node) {
        if (compact && node.getStyleClass().contains("page")) node.getStyleClass().add("compact");
        scroll.setContent(node);
        scroll.setVvalue(0);
    }

    private VBox sidebar() {
        var brand = new VBox(5, Ui.label("AppointEase", "brand"), Ui.label("APPOINTMENT MANAGEMENT", "brand-caption"));
        brand.getStyleClass().add("brand-block");
        var links = new VBox(5);
        for (var page : DesktopController.Page.values()) {
            var button = Ui.action(DesktopController.title(page), "nav-" + page.name().toLowerCase(java.util.Locale.ROOT), () -> navigate(page));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.getStyleClass().add("nav-button");
            navigation.put(page, button);
            links.getChildren().add(button);
        }
        var space = new VBox();
        VBox.setVgrow(space, Priority.ALWAYS);
        var footer = new VBox(9, Ui.text("LOCAL WORKSPACE", "sidebar-caption"),
                Ui.text(controller.time().zone().getId(), "sidebar-note"),
                Ui.text("Fictional data.\nResets when you close the app.", "sidebar-note"));
        footer.getStyleClass().add("sidebar-footer");
        var result = new VBox(20, brand, links, space, footer);
        result.setPrefWidth(214);
        result.setMinWidth(214);
        result.getStyleClass().add("sidebar");
        return result;
    }
}
