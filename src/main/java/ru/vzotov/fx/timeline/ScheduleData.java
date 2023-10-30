package ru.vzotov.fx.timeline;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

import java.time.LocalDate;

public class ScheduleData {

    public ScheduleData() {
    }

    public ScheduleData(LocalDate start, LocalDate finish, long value) {
        setStart(start);
        setFinish(finish);
        setValue(value);
    }

    /**
     * Node
     */
    private final ObjectProperty<Node> node = new SimpleObjectProperty<>();

    public Node getNode() {
        return node.get();
    }

    public ObjectProperty<Node> nodeProperty() {
        return node;
    }

    public void setNode(Node node) {
        this.node.set(node);
    }

    /**
     * Value
     */
    private final LongProperty value = new SimpleLongProperty();

    public long getValue() {
        return value.get();
    }

    public LongProperty valueProperty() {
        return value;
    }

    public void setValue(long value) {
        this.value.set(value);
    }

    /**
     * Start date
     */
    private final ObjectProperty<LocalDate> start = new SimpleObjectProperty<>();

    public LocalDate getStart() {
        return start.get();
    }

    public ObjectProperty<LocalDate> startProperty() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start.set(start);
    }

    /**
     * End date
     */
    private final ObjectProperty<LocalDate> finish = new SimpleObjectProperty<>();

    public LocalDate getFinish() {
        return finish.get();
    }

    public ObjectProperty<LocalDate> finishProperty() {
        return finish;
    }

    public void setFinish(LocalDate finish) {
        this.finish.set(finish);
    }
}
