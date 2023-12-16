package ru.vzotov.fx.timeline;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;
import ru.vzotov.fx.timeline.skin.TimelineSkin;

import java.util.function.BiConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class DefaultValueAxisBuilder implements ValueAxisBuilder<Double> {

    private final double paddingX;
    private final DoubleFunction<String> format;

    public DefaultValueAxisBuilder(DoubleFunction<String> format) {
        this(TimelineSkin.VALUE_AXIS_PADDING, format);
    }

    public DefaultValueAxisBuilder(double paddingX, DoubleFunction<String> format) {
        this.paddingX = paddingX;
        this.format = format;
    }

    public double getPaddingX() {
        return paddingX;
    }

    @Override
    public void buildTicks(double height, Double max,
                           DoubleUnaryOperator snapPositionY,
                           DoubleProperty viewportWidth,
                           BiConsumer<Line, Label> callback) {
        final double maxValue = max == null ? 0.0 : max;
        NiceScale axis = new NiceScale(0.0, maxValue);
        double space = axis.getTickSpacing();
        double tmin = axis.getNiceMin();
        double scale = height / maxValue;
        for (double v = tmin + space; v <= maxValue; v += space) {
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
