package ru.vzotov.fx.timeline;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.PathElement;
import ru.vzotov.fx.utils.Data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;


public abstract class Series<D> {

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
    private final ReadOnlyDoubleWrapper maxValue = new ReadOnlyDoubleWrapper(this, "maxValue", 0.0);

    public double getMaxValue() {
        return maxValue.get();
    }

    public ReadOnlyDoubleProperty maxValueProperty() {
        return maxValue.getReadOnlyProperty();
    }

    protected void setMaxValue(double maxValue) {
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
     * Обновляет кривую графика
     * @param parent
     * @param height
     * @return
     */
    public abstract Node updatePath(Timeline parent, double height);

    /**
     * Строит новую кривую
     * @param parent
     * @param height
     * @return
     */
    public abstract Node buildPath(Timeline parent, double height);


}
