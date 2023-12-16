package ru.vzotov.fx.timeline;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vzotov.fxtheme.FxTheme;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static ru.vzotov.fx.utils.LayoutUtils.anchor;
import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class DemoSchedule extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(DemoSchedule.class);

    public static void main(String[] args) {
        FxTheme.loadFonts();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Timeline<ScheduleData, Integer> timeline = new Timeline<>();
        timeline.setValueAxisBuilder(new IndexAxisBuilder(Integer::toString));

        List<CustomData> data = List.of(
                new CustomData("Item 1",
                        LocalDate.now().withMonth(Month.JANUARY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.MARCH.getValue()).withDayOfMonth(31),
                        1
                ),
                new CustomData("Item 2",
                        LocalDate.now().withMonth(Month.APRIL.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.JUNE.getValue()).withDayOfMonth(30),
                        1
                ),
                new CustomData("Item 3",
                        LocalDate.now().withMonth(Month.JULY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.OCTOBER.getValue()).withDayOfMonth(31),
                        1
                ),
                new CustomData("Item 4",
                        LocalDate.now().withMonth(Month.FEBRUARY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.APRIL.getValue()).withDayOfMonth(30),
                        2
                ),
                new CustomData("Item 5",
                        LocalDate.now().withMonth(Month.MAY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.JULY.getValue()).withDayOfMonth(31),
                        2
                ),
                new CustomData("Item 6",
                        LocalDate.now().withMonth(Month.AUGUST.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.NOVEMBER.getValue()).withDayOfMonth(30),
                        2
                )
        );
        LOG.info("Data {}", data);
        final ScheduleSeries<CustomData> series = new ScheduleSeries<>("demo", data) {
            @Override
            protected void updateNode(CustomData data, int index, boolean last, double x1, double x2, double y) {
                if (data.getNode() instanceof Group group) {
                    final Rectangle rectangle = (Rectangle) group.getProperties().get("rect");
                    group.setLayoutX(x1);
                    group.setLayoutY(y - rectangle.getHeight() / 2);
                    rectangle.setWidth(x2 - x1);
                } else super.updateNode(data, index, last, x1, x2, y);
            }

            @Override
            protected Node buildNode(CustomData data, int index, boolean last, double x1, double x2, double y) {
                final Rectangle rect = styled("data", new Rectangle(x2 - x1, 16));
                final Group g = new Group(rect,new Label(data.getLabel()));
                g.setLayoutX(x1);
                g.setLayoutY(y - 8);
                g.getProperties().put("rect", rect);
                return g;
            }
        };
        timeline.getSeries().setAll(Collections.singleton(series));

        final ScrollPane scrollPane = new ScrollPane(timeline);
        scrollPane.setFitToHeight(true);

        final AnchorPane anchorPane = styled("demo", new AnchorPane(scrollPane));

        anchor(scrollPane, 16.0, 16.0, 16.0, 16.0);

        Scene scene = new Scene(anchorPane, 1024, 800);
        scene.getStylesheets().addAll(FxTheme.stylesheet(), "timeline.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class CustomData extends ScheduleData {
        private final String label;

        public CustomData(String label, LocalDate start, LocalDate finish, int value) {
            super(start, finish, value);
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
