package ru.vzotov.fx.timeline;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vzotov.fx.utils.Data;
import ru.vzotov.fxtheme.FxTheme;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
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
        final Timeline timeline = new Timeline();

        List<ScheduleData> data = List.of(
                new ScheduleData(
                        LocalDate.now().withMonth(Month.JANUARY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.MARCH.getValue()).withDayOfMonth(31),
                        1L
                ),
                new ScheduleData(
                        LocalDate.now().withMonth(Month.APRIL.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.JUNE.getValue()).withDayOfMonth(30),
                        1L
                ),
                new ScheduleData(
                        LocalDate.now().withMonth(Month.JULY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.OCTOBER.getValue()).withDayOfMonth(31),
                        1L
                ),
                new ScheduleData(
                        LocalDate.now().withMonth(Month.FEBRUARY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.APRIL.getValue()).withDayOfMonth(30),
                        2L
                ),
                new ScheduleData(
                        LocalDate.now().withMonth(Month.MAY.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.JULY.getValue()).withDayOfMonth(31),
                        2L
                ),
                new ScheduleData(
                        LocalDate.now().withMonth(Month.AUGUST.getValue()).withDayOfMonth(1),
                        LocalDate.now().withMonth(Month.NOVEMBER.getValue()).withDayOfMonth(30),
                        2L
                )
        );
        LOG.info("Data {}", data);
        timeline.getSeries().setAll(new ScheduleSeries("demo", data));

        final ScrollPane scrollPane = new ScrollPane(timeline);

        final AnchorPane anchorPane = styled("demo", new AnchorPane(scrollPane));

        anchor(scrollPane, 16.0, 16.0, 16.0, 16.0);

        Scene scene = new Scene(anchorPane, 1024, 800);
        scene.getStylesheets().addAll(FxTheme.stylesheet(), "timeline.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
