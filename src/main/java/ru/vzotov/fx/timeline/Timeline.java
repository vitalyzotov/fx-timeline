package ru.vzotov.fx.timeline;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import ru.vzotov.calendar.domain.model.DateRange;
import ru.vzotov.fx.timeline.skin.TimelineSkin;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * @param <D> тип данных графика
 * @param <V> тип значений для данных
 */
public class Timeline<D, V extends Comparable<V>> extends Control implements TemporalAxis {

    private static final String DEFAULT_STYLE_CLASS = "timeline";

    public Timeline() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        series.addListener((InvalidationListener) it -> {
            hasSeries.set(!getSeries().isEmpty());
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimelineSkin<>(this);
    }

    /**
     * Value axis builder
     */
    private final ObjectProperty<ValueAxisBuilder<V>> valueAxisBuilder = new SimpleObjectProperty<>();

    public ValueAxisBuilder<V> getValueAxisBuilder() {
        return valueAxisBuilder.get();
    }

    public ObjectProperty<ValueAxisBuilder<V>> valueAxisBuilderProperty() {
        return valueAxisBuilder;
    }

    public void setValueAxisBuilder(ValueAxisBuilder<V> valueAxisBuilder) {
        this.valueAxisBuilder.set(valueAxisBuilder);
    }

    @Override
    public double getLocation(LocalDate date) {
        final long days = ChronoUnit.DAYS.between(getRange().start(), date);
        return getLeftGap() + days * getDayWidth();
    }

    @Override
    public LocalDate getDateAt(double x) {
        final long days = (long) ((x - getLeftGap()) / getDayWidth());
        return getRange().start().plusDays(days);
    }

    public void selectMonth(LocalDate date) {
        final DateRange<LocalDate> range = getRange();
        final DateRange<LocalDate> r = new DateRange<>(
                date.with(TemporalAdjusters.firstDayOfMonth()),
                date.with(TemporalAdjusters.lastDayOfMonth()));
        final DateRange<LocalDate> target = r.intersect(range, ChronoUnit.DAYS);
        if(target != null) {
            setSelectedRange(target);
        }
    }

    public void selectYear(LocalDate date) {
        final DateRange<LocalDate> range = getRange();
        final DateRange<LocalDate> r = new DateRange<>(
                date.with(TemporalAdjusters.firstDayOfYear()),
                date.with(TemporalAdjusters.lastDayOfYear()));
        final DateRange<LocalDate> target = r.intersect(range, ChronoUnit.DAYS);
        if(target != null) {
            setSelectedRange(target);
        }
    }

    /**
     * Данные для отрисовки графика
     */
    private final ObservableList<Series<? extends D, V>> series = FXCollections.observableArrayList();

    public ObservableList<Series<? extends D, V>> getSeries() {
        return series;
    }

    /**
     * Has series
     */
    private final ReadOnlyBooleanWrapper hasSeries = new ReadOnlyBooleanWrapper(this, "hasSeries", false);

    public boolean isHasSeries() {
        return hasSeries.get();
    }

    public ReadOnlyBooleanProperty hasSeriesProperty() {
        return hasSeries.getReadOnlyProperty();
    }

    /**
     * Expanded height
     */
    private final DoubleProperty expandedHeight = new SimpleDoubleProperty(400.0);

    public double getExpandedHeight() {
        return expandedHeight.get();
    }

    public DoubleProperty expandedHeightProperty() {
        return expandedHeight;
    }

    public void setExpandedHeight(double expandedHeight) {
        this.expandedHeight.set(expandedHeight);
    }

    /**
     * Отступ слева
     */
    private final DoubleProperty leftGap = new SimpleDoubleProperty(this, "leftGap", 50d);

    public double getLeftGap() {
        return leftGap.get();
    }

    public DoubleProperty leftGapProperty() {
        return leftGap;
    }

    public void setLeftGap(double leftGap) {
        this.leftGap.set(leftGap);
    }

    /**
     * Отступ справа
     */
    private final DoubleProperty rightGap = new SimpleDoubleProperty(this, "rightGap", 50d);

    public double getRightGap() {
        return rightGap.get();
    }

    public DoubleProperty rightGapProperty() {
        return rightGap;
    }

    public void setRightGap(double rightGap) {
        this.rightGap.set(rightGap);
    }

    /**
     * Ширина одного дня на таймлайне в пикселях
     */
    private final DoubleProperty dayWidth = new SimpleDoubleProperty(this, "dayWidth", 3.33d);

    public double getDayWidth() {
        return dayWidth.get();
    }

    public DoubleProperty dayWidthProperty() {
        return dayWidth;
    }

    public void setDayWidth(double dayWidth) {
        if (dayWidth < 1) {
            throw new IllegalArgumentException(
                    "dayWidth must not be less than 1 but was " + dayWidth);
        }

        this.dayWidth.set(dayWidth);
    }

    /**
     * Выбранный интервал
     */
    private final ObjectProperty<DateRange<LocalDate>> selectedRange = new SimpleObjectProperty<>(this, "selectedRange", null);

    public DateRange<LocalDate> getSelectedRange() {
        return selectedRange.get();
    }

    public ObjectProperty<DateRange<LocalDate>> selectedRangeProperty() {
        return selectedRange;
    }

    public void setSelectedRange(DateRange<LocalDate> selectedRange) {
        if (selectedRange != null) {
            selectedRange = selectedRange.within(getRange(), ChronoUnit.DAYS);
        }
        this.selectedRange.set(selectedRange);
    }

    /**
     * Интервал всего таймлайна
     */
    private final ObjectProperty<DateRange<LocalDate>> range = new SimpleObjectProperty<>(this, "range", new DateRange<>(LocalDate.now().minusYears(1), LocalDate.now()));

    /**
     * Интервал всего таймлайна
     */
    public DateRange<LocalDate> getRange() {
        return range.get();
    }

    /**
     * Интервал всего таймлайна (свойство)
     */
    public ObjectProperty<DateRange<LocalDate>> rangeProperty() {
        return range;
    }

    /**
     * Изменяет интервал всего таймлайна
     */
    public void setRange(DateRange<LocalDate> range) {
        if (!Objects.equals(getRange(), range)) {
            this.range.set(range);
        }
    }

}
