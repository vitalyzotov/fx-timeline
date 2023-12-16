package ru.vzotov.fx.timeline;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;
import ru.vzotov.fx.timeline.skin.TimelineSkin;

import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;

import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class IndexAxisBuilder implements ValueAxisBuilder<Integer> {

    private final double paddingX;
    private final IntFunction<String> format;

    public IndexAxisBuilder(IntFunction<String> format) {
        this(TimelineSkin.VALUE_AXIS_PADDING, format);
    }

    public IndexAxisBuilder(double paddingX, IntFunction<String> format) {
        this.paddingX = paddingX;
        this.format = format;
    }

    public double getPaddingX() {
        return paddingX;
    }

    @Override
    public void buildTicks(double height, Integer max, DoubleUnaryOperator snapPositionY, DoubleProperty viewportWidth, BiConsumer<Line, Label> callback) {
        final int maxValue = max == null ? 0 : max;
        double scale = height / maxValue;
        for (int v = 1; v <= maxValue; v++) {
            final double y = snapPositionY.applyAsDouble(height - v * scale) - .5;
            final Line tick = styled(new Line(0, y, 0, y), "tick");
            tick.endXProperty().bind(viewportWidth);

            Label tickLabel = new Label(format.apply(v));
            tickLabel.setManaged(true);
            tickLabel.setTranslateX(getPaddingX());
            tickLabel.setTranslateY(y - 6.0);

            callback.accept(tick, tickLabel);
        }
    }
}
