package ru.vzotov.fx.timeline;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import ru.vzotov.fx.utils.Data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class CurveSeries extends Series<Data<LocalDate, Node>, Double> {

    public CurveSeries() {
    }

    public CurveSeries(String name, Collection<Data<LocalDate, Node>> data) {
        super(name, data);
    }

    @Override
    public Node update(TemporalAxis temporalAxis, double height) {
        PathElement e = null;

        var maxRef = new AtomicReference<>(0.0);
        var sorted = getData().stream()
                .peek(d -> maxRef.set(Math.max(maxRef.get(), d.getValue())))
                .sorted(Comparator.comparing(Data::getX))
                .toList();
        double vmax = maxRef.get();
        setMaxValue(vmax);

        for (int i = 0, max = sorted.size() - 1; i <= max; i++) {
            var d = sorted.get(i);
            double x = temporalAxis.getLocation(d.getX());
            double y = height - height * d.getValue() / vmax;
            var current = getPathOfData(d);

            Circle circle = (Circle) d.getNode();
            if (circle != null) {
                circle.setCenterX(x);
                circle.setCenterY(y);
            }

            if (i == 0 && current instanceof MoveTo) {
                var mv = (MoveTo) current;
                mv.setX(x);
                mv.setY(y);
            } else if (i >= 2 && e instanceof QuadCurveTo) {
                QuadCurveTo pc = (QuadCurveTo) e;
                pc.setControlX(x);
                pc.setControlY(y);
            }
            if (i == max && current instanceof QuadCurveTo) {
                QuadCurveTo pc = (QuadCurveTo) current;
                pc.setX(x);
                pc.setY(y);
                pc.setControlX(x);
                pc.setControlY(y);
            }
            e = current;
        }
        return node;
    }

    @Override
    public Node build(TemporalAxis temporalAxis, double height) {
        PathElement e = null;
        final Path p = styled(new Path(), "series");
        final ObservableList<PathElement> elements = p.getElements();

        p.setManaged(false);
        Group g;
        node = g = new Group(p);
        node.setManaged(false);

        final AtomicReference<Double> maxRef = new AtomicReference<>(0.0);
        final List<Data<LocalDate, Node>> sorted = getData().stream()
                .peek(d -> maxRef.set(Math.max(maxRef.get(), d.getValue())))
                .sorted(Comparator.comparing(Data::getX))
                .toList();
        double vmax = maxRef.get();
        setMaxValue(vmax);

        for (int i = 0, max = sorted.size() - 1; i <= max; i++) {
            var d = sorted.get(i);
            double x = temporalAxis.getLocation(d.getX());
            double y = height - height * d.getValue() / vmax;
            buildNode(d, i, i == max, e, x, y);
            e = getPathOfData(d);
            elements.add(e);
            g.getChildren().add(d.getNode());
        }

        return node;
    }

    protected void buildNode(Data<LocalDate, Node> data, int index, boolean last, PathElement prev, double x, double y) {
        PathElement path;

        final Circle node = styled(new Circle(x, y, 4), "data");

        if (index == 0) {
            path = new MoveTo(x, y);
        } else {
            final QuadCurveTo curve = new QuadCurveTo(x, y, x, y);
            path = curve;

            if (index >= 2) {
                final QuadCurveTo pc = (QuadCurveTo) prev;
                pc.setControlX(x);
                pc.setControlY(y);
                pc.xProperty().bind(pc.controlXProperty().add(curve.controlXProperty()).divide(2.0));
                pc.yProperty().bind(pc.controlYProperty().add(curve.controlYProperty()).divide(2.0));
            }
        }
        data.setNode(node);
        setPathOfData(data, path);
    }

    protected PathElement getPathOfData(Data<LocalDate, Node> data) {
        return (PathElement) data.getNode().getProperties().get("path");
    }

    protected void setPathOfData(Data<LocalDate, Node> data, PathElement path) {
        data.getNode().getProperties().put("path", path);
    }
}
