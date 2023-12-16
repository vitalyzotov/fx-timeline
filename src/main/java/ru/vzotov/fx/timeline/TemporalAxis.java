package ru.vzotov.fx.timeline;

import java.time.LocalDate;

public interface TemporalAxis {

    /**
     * Возвращает координату для заданной даты
     *
     * @param date дата, для которой нужно определить координату
     * @return координата
     */
    double getLocation(LocalDate date);

    /**
     * Возвращает дату для заданной координаты
     *
     * @param x координата
     * @return дата для переданной координаты
     */
    LocalDate getDateAt(double x);
}
