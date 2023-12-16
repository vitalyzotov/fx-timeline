package ru.vzotov.fx.timeline;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import java.util.Collection;
import java.util.List;

import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class ScheduleSeries<T extends ScheduleData> extends Series<T, Integer> {

    public ScheduleSeries() {
    }

    public ScheduleSeries(String name, Collection<T> data) {
        super(name, data);
    }

    @Override
    public Node build(TemporalAxis temporalAxis, double height) {
        final Group g;
        node = g = new Group();
        g.setManaged(false);

        final List<T> list = getData();
        int vmax = updateMaxValue(list);

        for (int i = 0, max = list.size() - 1; i <= max; i++) {
            final T d = list.get(i);
            final double x1 = temporalAxis.getLocation(d.getStart());
            final double x2 = temporalAxis.getLocation(d.getFinish());
            final double y = height - height * d.getValue() / (double) vmax;
            final Node n = buildNode(d, i, i == max, x1, x2, y);
            d.setNode(n);
            g.getChildren().add(n);
        }

        return node;
    }

    private int updateMaxValue(List<T> list) {
        int vmax = list.stream()
                .mapToInt(T::getValue)
                .max().orElse(0);
        setMaxValue(vmax);
        return vmax;
    }

    @Override
    public Node update(TemporalAxis temporalAxis, double height) {
        final List<T> list = getData();
        int vmax = updateMaxValue(list);
        for (int i = 0, max = list.size() - 1; i <= max; i++) {
            final T d = list.get(i);
            final double x1 = temporalAxis.getLocation(d.getStart());
            final double x2 = temporalAxis.getLocation(d.getFinish());
            final double y = height - height * d.getValue() / (double) vmax;
            updateNode(d, i, i == max, x1, x2, y);
        }
        return node;
    }

    protected void updateNode(T data, int index, boolean last, double x1, double x2, double y) {
        if (data.getNode() instanceof Rectangle rectangle) {
            rectangle.setX(x1);
            rectangle.setWidth(x2 - x1);
            rectangle.setY(y - rectangle.getHeight() / 2);
        } else {
            throw new IllegalStateException("Unknown node type");
        }
    }

    protected Node buildNode(T data, int index, boolean last, double x1, double x2, double y) {
        return styled("data", new Rectangle(x1, y - 8, x2 - x1, 16));
    }


}
