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
import java.util.Objects;

public class Timeline extends Control {

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
     * Возвращает координату x для заданной даты
     *
     * @param date дата, для которой нужно определить координату
     * @return координата x
     */
    public double getLocation(LocalDate date) {
        final long days = ChronoUnit.DAYS.between(getRange().start(), date);
        return getLeftGap() + days * getDayWidth();
    }

    /**
     * Возвращает дату для заданной координаты
     *
     * @param x координата
     * @return дата для переданной координаты
     */
    public LocalDate getDateAt(double x) {
        final long days = (long) ((x - getLeftGap()) / getDayWidth());
        return getRange().start().plusDays(days);
    }

    public void selectMonth(LocalDate date) {
        setSelectedRange(new DateRange<>(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth())));
    }

    /**
     * Данные для отрисовки графика
     */
    private final ObservableList<Series> series = FXCollections.observableArrayList();

    public ObservableList<Series> getSeries() {
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
