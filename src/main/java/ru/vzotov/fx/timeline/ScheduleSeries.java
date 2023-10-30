package ru.vzotov.fx.timeline;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static ru.vzotov.fx.utils.LayoutUtils.styled;

public class ScheduleSeries extends Series<ScheduleData> {

    public ScheduleSeries() {
    }

    public ScheduleSeries(String name, Collection<ScheduleData> data) {
        super(name, data);
    }

    @Override
    public Node buildPath(Timeline parent, double height) {
        final Group g;
        node = g = new Group();
        g.setManaged(false);

        final AtomicReference<Double> maxRef = new AtomicReference<>(0.0);

        final List<ScheduleData> sorted = getData().stream()
                .peek(d -> maxRef.set(Math.max(maxRef.get(), d.getValue())))
                .sorted(Comparator.comparing(ScheduleData::getStart))
                .toList();
        double vmax = maxRef.get();
        setMaxValue(vmax);

        for (int i = 0, max = sorted.size() - 1; i <= max; i++) {
            final ScheduleData d = sorted.get(i);
            double x1 = parent.getLocation(d.getStart());
            double x2 = parent.getLocation(d.getFinish());
            double y = height - height * d.getValue() / vmax;
            g.getChildren().add(buildNode(d, i, i == max, x1, x2, y));
        }

        return node;
    }

    @Override
    public Node updatePath(Timeline parent, double height) {
        AtomicLong maxRef = new AtomicLong(0);
        List<ScheduleData> sorted = getData().stream()
                .peek(d -> maxRef.set(Math.max(maxRef.get(), d.getValue())))
                .sorted(Comparator.comparing(ScheduleData::getStart))
                .toList();
        long vmax = maxRef.get();
        setMaxValue((double) vmax);

        for (int i = 0, max = sorted.size() - 1; i <= max; i++) {
            ScheduleData d = sorted.get(i);
            double x1 = parent.getLocation(d.getStart());
            double x2 = parent.getLocation(d.getFinish());
            double y = height - height * d.getValue() / vmax;

            if (d.getNode() instanceof Rectangle rectangle) {
                rectangle.setX(x1);
                rectangle.setWidth(x2 - x1);
                rectangle.setY(y - rectangle.getHeight() / 2);
            }
        }
        return node;
    }

    private Node buildNode(ScheduleData data, int index, boolean last, double x1, double x2, double y) {
        final Rectangle node = styled("data", new Rectangle(x1, y - 8, x2 - x1, 16));
        data.setNode(node);
        return node;
    }


}
