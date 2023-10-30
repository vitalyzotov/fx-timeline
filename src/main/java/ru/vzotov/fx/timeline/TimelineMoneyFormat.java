package ru.vzotov.fx.timeline;

import java.text.NumberFormat;

public class TimelineMoneyFormat implements MoneyFormat {

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();

    @Override
    public String format(double value) {
        return numberFormat.format(value);
    }
}
