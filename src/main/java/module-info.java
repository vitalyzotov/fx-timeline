import ru.vzotov.fx.timeline.MoneyFormat;
import ru.vzotov.fx.timeline.TimelineMoneyFormat;

module ru.vzotov.fx.timeline {
    requires javafx.controls;
    requires calendar.model;
    requires org.slf4j;
    requires ru.vzotov.fx.utils;
    requires fxtheme.dark;

    opens ru.vzotov.fx.timeline to javafx.fxml;
    exports ru.vzotov.fx.timeline;
    uses MoneyFormat;
    provides MoneyFormat with TimelineMoneyFormat;
}
