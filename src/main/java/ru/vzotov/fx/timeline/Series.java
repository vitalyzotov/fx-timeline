package ru.vzotov.fx.timeline;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.Collection;
import java.util.Collections;


public abstract class Series<D, V extends Comparable<V>> {

    public Series() {
        this("", Collections.emptyList());
    }

    public Series(String name, Collection<D> data) {
        setName(name);
        getData().setAll(data);
    }

    protected Node node;

    public Node getNode() {
        return node;
    }

    /**
     * Max value in series
     */
    private final ReadOnlyObjectWrapper<V> maxValue = new ReadOnlyObjectWrapper<>(this, "maxValue", null);

    public V getMaxValue() {
        return maxValue.get();
    }

    public ReadOnlyObjectProperty<V> maxValueProperty() {
        return maxValue.getReadOnlyProperty();
    }

    protected void setMaxValue(V maxValue) {
        this.maxValue.set(maxValue);
    }

    /**
     * Series name
     */
    private final StringProperty name = new SimpleStringProperty();

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Series data
     */
    private final ObservableList<D> data = FXCollections.observableArrayList();

    public ObservableList<D> getData() {
        return data;
    }

    /**
     * Обновляет график
     *
     * @param temporalAxis шкала времени
     * @param height высота графика
     * @return
     */
    @SuppressWarnings("UnusedReturnValue")
    public abstract Node update(TemporalAxis temporalAxis, double height);

    /**
     * Строит новый график
     *
     * @param temporalAxis шкала времени
     * @param height высота графика
     * @return
     */
    public abstract Node build(TemporalAxis temporalAxis, double height);


}
