import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class ColorOcTree<T> {
    public enum OcTreeQuadrant {
        UPFRONTLEFT(),
        UPFRONTRIGHT(),
        UPBACKLEFT(),
        UPBACKRIGHT(),
        DOWNFRONTLEFT(),
        DOWNFRONTRIGHT(),
        DOWNBACKLEFT(),
        DOWNBACKRIGHT()
    }

    private Point3D min;
    private Point3D max;
    private Collection<T> contents;
    private ColorOcTree<T> parent;
    private Map<OcTreeQuadrant, ColorOcTree<T>> children;

    public ColorOcTree(Point3D min, Point3D max, Function<T, Point3D> coordinator) {
        this(min, max, coordinator, null);
    }

    private ColorOcTree(Point3D min, Point3D max, Function<T, Point3D> coordinator, ColorOcTree<T> parent) {
        this.min = min;
        this.max = max;
        this.parent = parent;
    }
}