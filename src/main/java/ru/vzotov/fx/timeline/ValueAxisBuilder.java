package ru.vzotov.fx.timeline;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;

import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;

/**
 * @param <V> тип значений шкалы
 */
public interface ValueAxisBuilder<V> {

    void buildTicks(double height, V max,
                    DoubleUnaryOperator snapPositionY,
                    DoubleProperty viewportWidth,
                    BiConsumer<Line, Label> callback);

}
