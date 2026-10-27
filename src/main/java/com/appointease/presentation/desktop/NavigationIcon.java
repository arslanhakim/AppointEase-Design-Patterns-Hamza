package com.appointease.presentation.desktop;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** Small local vector vocabulary: one stroke weight, no font or image dependency. */
final class NavigationIcon extends StackPane {
    private final SVGPath stroke = new SVGPath();

    private NavigationIcon(String path) {
        setMinSize(22, 22);
        setPrefSize(22, 22);
        setMaxSize(22, 22);
        setMouseTransparent(true);
        getStyleClass().add("navigation-icon");
        stroke.getStyleClass().add("navigation-icon-stroke");
        stroke.setContent(path);
        getChildren().add(stroke);
    }

    static NavigationIcon page(DesktopController.Page page) {
        return new NavigationIcon(switch (page) {
            case OVERVIEW -> "M3 3H9V9H3Z M15 3H21V9H15Z M3 15H9V21H3Z M15 15H21V21H15Z";
            case APPOINTMENTS -> "M7 5H21 M7 12H21 M7 19H21 M3 5H3.1 M3 12H3.1 M3 19H3.1";
            case NEW_APPOINTMENT -> "M5 3H19Q21 3 21 5V19Q21 21 19 21H5Q3 21 3 19V5Q3 3 5 3Z M12 7V17 M7 12H17";
            case PROVIDERS -> "M8 3A4 4 0 1 1 8 11A4 4 0 1 1 8 3 M1 21V19Q1 14 8 14Q15 14 15 19V21 M17 4Q22 4 22 8Q22 12 17 12 M18 15Q23 16 23 20V21";
            case SCHEDULE -> "M5 5H19Q21 5 21 7V19Q21 21 19 21H5Q3 21 3 19V7Q3 5 5 5Z M7 2V8 M17 2V8 M3 11H21 M8 15H8.1 M12 15H12.1 M16 15H16.1 M8 18H8.1 M12 18H12.1";
            case ACTIVITY -> "M12 3A9 9 0 1 1 3 12 M3 3V9H9 M12 7V12L16 14";
            case CUSTOMERS -> "M12 3A4 4 0 1 1 12 11A4 4 0 1 1 12 3 M4 21V19Q4 14 12 14Q20 14 20 19V21";
            case SERVICES -> "M5 3H19V21H5Z M9 8H15 M9 12H15 M9 16H13";
        });
    }

    static NavigationIcon sidebar() { return new NavigationIcon(""); }

    void expanded(boolean expanded) {
        // Panel-left-close / panel-left-open: a full arrow stays legible beside the narrow panel.
        // The path plus its 1.5px stroke occupies 19.5px within the unchanged icon holder.
        stroke.setContent("M3 1H17Q19 1 19 3V17Q19 19 17 19H3Q1 19 1 17V3Q1 1 3 1Z M6 1V19 "
                + (expanded ? "M16 10H9 M12 6.5L8.5 10L12 13.5" : "M9 10H16 M12.5 6.5L16 10L12.5 13.5"));
    }
}
