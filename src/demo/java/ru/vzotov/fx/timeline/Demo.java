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
import ru.vzotov.fx.utils.LayoutUtils;
import ru.vzotov.fxtheme.FxTheme;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static ru.vzotov.fx.utils.LayoutUtils.anchor;
import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class Demo extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        FxTheme.loadFonts();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Timeline timeline = new Timeline();
        List<Data<LocalDate, Node>> data = new ArrayList<>();
        for(int i = 0; i < 16; i++) {
            LocalDate month = YearMonth.now().minusMonths(i).atEndOfMonth();
            data.add(new Data<>(month, Math.abs(Math.random()) * 30000d));
        }
        LOG.info("Data {}", data);
        timeline.getSeries().setAll(new CurveSeries("demo", data));

        final ScrollPane scrollPane = new ScrollPane(timeline);


        final AnchorPane anchorPane = styled("demo", new AnchorPane(scrollPane));

        anchor(scrollPane, 16.0, 16.0, 16.0, 16.0);

        Scene scene = new Scene(anchorPane, 1024, 800);
        scene.getStylesheets().addAll(FxTheme.stylesheet(), "timeline.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
