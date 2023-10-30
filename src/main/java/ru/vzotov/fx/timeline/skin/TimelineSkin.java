package ru.vzotov.fx.timeline.skin;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vzotov.calendar.domain.model.DateRange;
import ru.vzotov.fx.timeline.MoneyFormat;
import ru.vzotov.fx.timeline.NiceScale;
import ru.vzotov.fx.timeline.Series;
import ru.vzotov.fx.timeline.Timeline;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import static ru.vzotov.fx.utils.LayoutUtils.styled;


public class TimelineSkin<T extends Timeline> extends SkinBase<T> {

    private static final Logger log = LoggerFactory.getLogger(TimelineSkin.class);

    public static final double SELECTION_HANDLE_WIDTH = 10.0;
    public static final double SELECTION_LABELS_PADDING = 8.0;

    private static final int HEIGHT_EXPANDED = 400;
    private static final double TOP_PADDING = 16;
    private static final double VALUE_AXIS_PADDING = 16;
    private static final double BASELINE_PADDING = TOP_PADDING * 1.5;
    private static final Duration DURATION = Duration.millis(300);

    private static final String MONTH_LINE_STYLE_CLASS = "month-line";
    private static final String MONTH_LABEL_STYLE_CLASS = "month-label";
    private static final String SELECTION_FROM_STYLE_CLASS = "selection-from";
    private static final String SELECTION_TO_STYLE_CLASS = "selection-to";


    private final DateTimeFormatter monthLabelFormatter = DateTimeFormatter.ofPattern("LLLL yyyy");
    private final MoneyFormat moneyFormatter = ServiceLoader.load(MoneyFormat.class).findFirst().orElseThrow();

    private final Group baselineGroup;
    private final TranslateTransition moveBaselineToCenter;
    private final TranslateTransition moveBaselineToBottom;

    private final Line baseline;

    private final Rectangle selection;

    private final Region selectionHandleFrom;

    private final Region selectionHandleTo;

    private final List<Line> lines = new ArrayList<>();

    private final List<Label> labels = new ArrayList<>();

    private final Group valueAxis;
    private final List<Line> valueTicks = new ArrayList<>();
    private final List<Label> valueTickLabels = new ArrayList<>();

    private final ObservableDoubleValue chartHeight;

    private final DoubleProperty viewportWidth = new SimpleDoubleProperty(this, "viewportWidth", 0.0);

    private final double initialPrefHeight;

    private final DateTimeFormatter selectionLabelFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final Label fromLabel = new Label("dd.mm.yyyy");
    private final Label toLabel = new Label("dd.mm.yyyy");

    public double getViewportWidth() {
        return viewportWidth.get();
    }

    public DoubleProperty viewportWidthProperty() {
        return viewportWidth;
    }

    public TimelineSkin(T control) {
        super(control);

        baselineGroup = new Group();
        baselineGroup.setManaged(false);
        getChildren().add(baselineGroup);

        final DoubleBinding baselineHeight = Bindings.createDoubleBinding(() ->
                baselineGroup.getLayoutBounds().getHeight(), baselineGroup.layoutBoundsProperty());

        chartHeight = control.heightProperty().subtract(BASELINE_PADDING + TOP_PADDING).subtract(baselineHeight);
        control.setSnapToPixel(true);

        selectionHandleFrom = createSelectionHandle(SELECTION_FROM_STYLE_CLASS);
        selectionHandleTo = createSelectionHandle(SELECTION_TO_STYLE_CLASS);

        // ================================================================================ selection rectangle
        selection = styled(new Rectangle(), "selection");
        selection.setVisible(false);
        selection.setManaged(false);
        selection.setMouseTransparent(true);
        //selection.setX(0);
        selection.heightProperty().bind(control.heightProperty());
        selection.xProperty().bind(selectionHandleFrom.layoutXProperty());
        selection.widthProperty().bind(selectionHandleTo.layoutXProperty().add(SELECTION_HANDLE_WIDTH).subtract(selectionHandleFrom.layoutXProperty()));
        getChildren().add(selection);

        fromLabel.setManaged(false);
        fromLabel.setMouseTransparent(true);
        fromLabel.layoutXProperty().bind(selectionHandleFrom.layoutXProperty().subtract(fromLabel.widthProperty()).subtract(SELECTION_LABELS_PADDING));
        fromLabel.setLayoutY(0);
        getChildren().add(fromLabel);

        toLabel.setManaged(false);
        toLabel.setMouseTransparent(true);
        toLabel.layoutXProperty().bind(selectionHandleTo.layoutXProperty().add(SELECTION_HANDLE_WIDTH).add(SELECTION_LABELS_PADDING));
        toLabel.setLayoutY(0);
        getChildren().add(toLabel);

        moveBaselineToCenter = new TranslateTransition(DURATION, baselineGroup);
        moveBaselineToCenter.toYProperty().bind(control.heightProperty()
                .subtract(baselineHeight)
                .divide(2.0)
        );
        moveBaselineToCenter.toYProperty().addListener(it -> {
            if (!control.isHasSeries()) moveBaselineToCenter.playFromStart();
        });

        moveBaselineToBottom = new TranslateTransition(DURATION, baselineGroup);
        moveBaselineToBottom.toYProperty().bind(chartHeightProperty());
        moveBaselineToBottom.toYProperty().addListener(it -> {
            if (control.isHasSeries()) moveBaselineToBottom.playFromStart();
        });

        baseline = styled(new Line(), "baseline");
        baseline.setManaged(false);
        baseline.setMouseTransparent(true);
        //baseline.setOpacity(0);
        baselineGroup.getChildren().add(baseline);

        // ====================================================================================  value axis
        Line valueAxisLine = styled(new Line(), "axis");
        valueAxisLine.setStartX(VALUE_AXIS_PADDING - .5);
        valueAxisLine.endXProperty().bind(valueAxisLine.startXProperty());
        valueAxisLine.endYProperty().bind(baselineGroup.translateYProperty());

        valueAxis = new Group(valueAxisLine);
        valueAxis.setAutoSizeChildren(true);
        valueAxis.setManaged(false);
        valueAxis.visibleProperty().bind(control.hasSeriesProperty());
        valueAxis.setTranslateY(TOP_PADDING);
        getChildren().add(valueAxis);

        // ====================================================================================  date axis

        createStaticLines();

        control.rangeProperty().addListener(it -> {
            createStaticLines();
            control.requestLayout();
        });

        control.selectedRangeProperty().addListener((observable, oldValue, newValue) -> {
            updateSelectionLabels(newValue);
        });

        control.selectedRangeProperty().addListener(it -> {
            updateSelectionLabels(control.getSelectedRange());
            control.requestLayout();
        });

        control.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.isStillSincePress()) control.selectMonth(control.getDateAt(evt.getX()));
        });

        control.getSeries().addListener((ListChangeListener<? super Series>) c -> {
            while (c.next()) {
                for (Series s : c.getRemoved()) {
                    var p = s.getNode();
                    if (p != null) getChildren().remove(p);
                }

                buildPathForSeries(c.getAddedSubList());
            }
            createValueTicks();
            control.requestLayout();
        });
        rebuildPathForSeries();
        createValueTicks();
        registerInvalidationListener(chartHeightProperty(), it -> {
            createValueTicks();
            rebuildPathForSeries();
        });

        initialPrefHeight = control.getPrefHeight();
        log.debug("Initial pref height = {}", initialPrefHeight);
        control.hasSeriesProperty().addListener(it -> {
            var has = control.isHasSeries();
            moveBaseline(control.isHasSeries());

            final javafx.animation.Timeline timeline = new javafx.animation.Timeline();
            final ObservableList<KeyFrame> frames = timeline.getKeyFrames();
            frames.add(new KeyFrame(DURATION, new KeyValue(control.prefHeightProperty(), has ? HEIGHT_EXPANDED : initialPrefHeight)));
            timeline.setOnFinished(finish -> {
                for (Series s : control.getSeries()) {
                    s.updatePath(control, getChartHeight());
                }
                createValueTicks();
                control.requestLayout();
            });
            timeline.play();
        });
        moveBaseline(false);

        control.parentProperty().addListener(it -> {
            bindAxisToScrollPane();
        });
        bindAxisToScrollPane();
        updateSelectionLabels(control.getSelectedRange());
    }

    /**
     * Полностью переделывает ноды для графиков
     */
    private void rebuildPathForSeries() {
        T control = getSkinnable();
        final double height = getChartHeight();

        for (Series s : control.getSeries()) {
            Node p = s.getNode();
            if (p != null) {
                s.updatePath(control, height);
            } else {
                getChildren().add(s.buildPath(control, height));
            }
        }

    }

    private void buildPathForSeries(Collection<? extends Series> c) {
        T control = getSkinnable();
        for (Series s : c) {
            var p = s.getNode();
            if (p == null) {
                getChildren().add(s.buildPath(control, getChartHeight()));
            }
        }
    }

    private void updateSelectionLabels(DateRange<LocalDate> range) {
        if (range == null) {
            fromLabel.setText("");
            toLabel.setText("");
        } else {
            fromLabel.setText(range.start().format(selectionLabelFormatter));
            toLabel.setText(range.finish().format(selectionLabelFormatter));
        }
    }

    public double getChartHeight() {
        return chartHeight.get();
    }

    public ObservableDoubleValue chartHeightProperty() {
        return chartHeight;
    }

    private void bindAxisToScrollPane() {
        T control = getSkinnable();
        ScrollPane scrollPane = findScrollPane(control.getParent());
        if (scrollPane != null) {
            viewportWidthProperty().unbind();
            viewportWidthProperty().bind(
                    Bindings.createDoubleBinding(() -> scrollPane.getViewportBounds().getWidth(),
                            scrollPane.viewportBoundsProperty()));

            valueAxis.translateXProperty().unbind();
            valueAxis.translateXProperty().bind(Bindings.createDoubleBinding(() ->
                            snapPositionX(scrollPane.getHvalue() * (control.getWidth() - getViewportWidth())),
                    scrollPane.hvalueProperty(), control.widthProperty(), viewportWidthProperty()));
        }
    }

    private ScrollPane findScrollPane(Parent parent) {
        if (parent == null || parent instanceof ScrollPane) {
            return (ScrollPane) parent;
        } else {
            return findScrollPane(parent.getParent());
        }
    }

    private void moveBaseline(boolean toBottom) {
        if (toBottom) {
            moveBaselineToBottom.playFromStart();
        } else {
            moveBaselineToCenter.playFromStart();
        }
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        T control = getSkinnable();
        double w = control.getLeftGap() + snapSizeX(control.getLocation(control.getRange().finish()) + control.getRightGap());
        return w;
    }

    private Region createSelectionHandle(final String styleClass) {
        Region rect = styled(new Region(), styleClass);
        rect.setVisible(false);
        rect.setManaged(false);
        rect.setMouseTransparent(false);
        rect.setCursor(Cursor.H_RESIZE);

        final double handleRadius = SELECTION_HANDLE_WIDTH;
        final Wrapper<Point2D> mouseLocation = new Wrapper<>();
        setUpDragging(rect, mouseLocation);

        rect.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double newX = rect.getLayoutX() + deltaX;
                double newMaxX = newX + rect.getWidth();
                if (newX >= handleRadius
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    rect.relocate(newX, 0);
                    if (rect == selectionHandleFrom) {
                        fromLabel.setText(getSkinnable().getDateAt(newX).format(selectionLabelFormatter));
                    } else if (rect == selectionHandleTo) {
                        toLabel.setText(getSkinnable().getDateAt(newX + SELECTION_HANDLE_WIDTH).format(selectionLabelFormatter));
                    }
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });
        getChildren().add(rect);

        return rect;
    }

    private void createValueTicks() {
        T control = getSkinnable();
        double height = getChartHeight();

        valueAxis.getChildren().removeAll(valueTicks);
        valueAxis.getChildren().removeAll(valueTickLabels);
        valueTicks.clear();
        valueTickLabels.clear();

        double max = 0.0;
        for (Series s : control.getSeries()) {
            max = Math.max(max, s.getMaxValue());
        }
        NiceScale axis = new NiceScale(0.0, max);
        double space = axis.getTickSpacing();
        double tmin = axis.getNiceMin();
        double scale = height / max;
        for (double v = tmin + space; v <= max; v += space) {
            final double y = snapPositionY(height - v * scale) - .5;
            final Line tick = styled(new Line(0, y, 0, y), "tick");
            tick.endXProperty().bind(viewportWidthProperty());
            valueTicks.add(tick);
            valueAxis.getChildren().add(tick);

            Label tickLabel = new Label(moneyFormatter.format(v));
            tickLabel.setManaged(true);
            tickLabel.setTranslateX(VALUE_AXIS_PADDING);
            tickLabel.setTranslateY(y - 6.0);
            valueTickLabels.add(tickLabel);
            valueAxis.getChildren().add(tickLabel);

        }

    }

    private void createStaticLines() {
        T control = getSkinnable();
        baselineGroup.getChildren().removeAll(lines);
        baselineGroup.getChildren().removeAll(labels);
        lines.clear();
        labels.clear();

        final long length = control.getRange().width(ChronoUnit.MONTHS) + 1; // на 1 линию больше чем умещается месяцев

        log.debug("Create {} ticks and labels", length);

        for (int i = 0; i < length; i++) {
            createLine(MONTH_LINE_STYLE_CLASS);
            createLabel(MONTH_LABEL_STYLE_CLASS);
        }
    }

    private void createLabel(String styleClass) {
        Label label = styled(new Label(), styleClass);
        label.setManaged(false);
        label.setMouseTransparent(true);
        labels.add(label);
        baselineGroup.getChildren().add(label);
    }

    private void createLine(String styleClass) {
        Line line = styled(new Line(), styleClass);
        line.setManaged(false);
        line.setMouseTransparent(true);
        lines.add(line);
        baselineGroup.getChildren().add(line);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        T control = getSkinnable();
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        layoutBaseline(contentX, contentY, contentWidth, contentHeight);
        layoutLines(contentX, contentY, contentWidth, contentHeight);
        baselineGroup.relocate(contentX, TOP_PADDING);

        layoutSelection(contentX, contentY, contentWidth, contentHeight);

        for (Series s : control.getSeries()) {
            var p = s.getNode();
            if (p != null) {
                p.setTranslateX(contentX);
                p.setTranslateY(TOP_PADDING);
            }
        }
    }

    protected void layoutSelection(double contentX, double contentY, double contentWidth, double contentHeight) {
        final T control = getSkinnable();

        final LocalDate selectedFrom = control.getSelectedRange() == null ? null : control.getSelectedRange().start();
        final LocalDate selectedTo = control.getSelectedRange() == null ? null : control.getSelectedRange().finish();
        final boolean visible = selectedFrom != null && selectedTo != null && !selectedFrom.isAfter(selectedTo);

        selection.setVisible(visible);
        selectionHandleFrom.setVisible(visible);
        selectionHandleTo.setVisible(visible);
        if (visible) {
            final double x1 = snapPositionX(contentX + control.getLocation(selectedFrom));
            final double x2 = snapPositionX(contentX + control.getLocation(selectedTo.plusDays(1)));
            selection.toFront();

            selectionHandleFrom.resizeRelocate(x1, 0, SELECTION_HANDLE_WIDTH, contentHeight);
            selectionHandleFrom.toFront();

            selectionHandleTo.resizeRelocate(x2 - selectionHandleTo.getWidth(), 0, SELECTION_HANDLE_WIDTH, contentHeight);
            selectionHandleTo.toFront();

            double w = fromLabel.prefWidth(contentHeight);
            double h = fromLabel.prefWidth(contentWidth);
            fromLabel.resize(w, h);
            fromLabel.toFront();

            w = toLabel.prefWidth(contentHeight);
            h = toLabel.prefWidth(contentWidth);
            toLabel.resize(w, h);
            toLabel.toFront();
        }
    }

    protected void layoutBaseline(double contentX, double contentY, double contentWidth, double contentHeight) {
        final double y = .5;
        baseline.setStartX(0);
        baseline.setStartY(y);
        baseline.setEndX(snapPositionX(contentWidth));
        baseline.setEndY(y);
    }

    protected void layoutLines(double contentX, double contentY, double contentWidth, double contentHeight) {
        final T control = getSkinnable();
        final int lineCount = lines.size();

        final LocalDate fromDate = control.getRange().start();
        final LocalDate toDate = control.getRange().finish();

        final double centerY = .5;
        final double startY = centerY + 10;
        final double labelY = centerY + 15;

        for (int i = 0; i < lineCount; i++) {
            final Line line = lines.get(i);
            final Label text = labels.get(i);
            final LocalDate date = fromDate.plusMonths(i).withDayOfMonth(1);
            final boolean visible = !(date.isBefore(fromDate) || date.isAfter(toDate));

            line.setVisible(visible);
            text.setVisible(visible);

            if (!visible) continue;

            final double x = snapPositionX(control.getLocation(date)) + .5;
            line.setStartX(x);
            line.setStartY(startY);
            line.setEndX(x);
            line.setEndY(centerY);
            text.setText(monthLabelFormatter.format(date));
            text.autosize();
            text.relocate(x, labelY);
        }
    }

    private void setUpDragging(Region handle, Wrapper<Point2D> mouseLocation) {

        handle.setOnDragDetected(event -> {
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        handle.setOnMouseReleased(event -> {
            T control = getSkinnable();
            mouseLocation.value = null;
            if (handle == selectionHandleFrom) {
                control.setSelectedRange(
                        control.getSelectedRange().startingAt(control.getDateAt(selectionHandleFrom.getLayoutX()))
                );
            } else if (handle == selectionHandleTo) {
                control.setSelectedRange(
                        control.getSelectedRange().finishingAt(control.getDateAt(selectionHandleTo.getLayoutX() + SELECTION_HANDLE_WIDTH))
                );
            }
        });
    }

    static class Wrapper<T> {
        T value;
    }
}
