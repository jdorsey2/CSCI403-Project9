import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

public class OcTree<T> {
    private static int MAX_PER_NODE = 8;

    public enum OcTreeOctant {
        LEFTBACKDOWN(),
        LEFTBACKUP(),
        LEFTFRONTDOWN(),
        LEFTFRONTUP(),
        RIGHTBACKDOWN(),
        RIGHTBACKUP(),
        RIGHTFRONTDOWN(),
        RIGHTFRONTUP()
    }

    private Point3D min;
    private Point3D max;
    private Function<T, Point3D> coordinator;
    private Collection<T> contents;
    private OcTree<T> parent;
    private Map<OcTreeOctant, OcTree<T>> children;

    public OcTree(Point3D min, Point3D max, Function<T, Point3D> coordinator) {
        this(min, max, coordinator, null);
    }

    private OcTree(Point3D min, Point3D max, Function<T, Point3D> coordinator, OcTree<T> parent) {
        this.min = min;
        this.max = max;
        this.parent = parent;
        this.coordinator = coordinator;
        this.contents = new HashSet<>();
    }

    public OcTree<T> getChild(OcTreeOctant o) {
        return children.get(o);
    }

    public OcTree<T> getParent() {
        return parent;
    }

    public void add(T obj) {
        if (contents.size() < MAX_PER_NODE) {
            if (children == null) {
                contents.add(obj);
            } else {
                for (OcTreeOctant o : children.keySet()) {
                    Point3D point = coordinator.apply(obj);
                    if (Point3D.inRange(children.get(o).min, children.get(o).max, point)) {
                        children.get(o).add(obj);
                        break;
                    }
                }
            }
        } else {
            Map<T, Point3D> pointMap = new HashMap<>();
            contents.forEach(item -> pointMap.put(item, coordinator.apply(item)));
            children = new HashMap<>();

            double sideLength = max.divide(2).getX() - min.getX();
            Point3D center = max.divide(2);

            Point3D leftBackDownMin = min;
            Point3D leftBackDownMax = center;
            OcTree<T> leftBackDown = new OcTree<>(leftBackDownMin, leftBackDownMax, coordinator, this);

            Point3D leftBackUpMin = min.add(new Point3D(0, 0, sideLength));
            Point3D leftBackUpMax = center.add(new Point3D(0, 0, sideLength));
            OcTree<T> leftBackUp = new OcTree<>(leftBackUpMin, leftBackUpMax, coordinator, this);

            Point3D leftFrontDownMin = min.add(new Point3D(0, sideLength, 0));
            Point3D leftFrontDownMax = center.add(new Point3D(0, sideLength, 0));
            OcTree<T> leftFrontDown = new OcTree<>(leftFrontDownMin, leftFrontDownMax, coordinator, this);

            Point3D leftFrontUpMin = min.add(new Point3D(0, sideLength, sideLength));
            Point3D leftFrontUpMax = center.add(new Point3D(0, sideLength, sideLength));
            OcTree<T> leftFrontUp = new OcTree<>(leftFrontUpMin, leftFrontUpMax, coordinator, this);

            Point3D rightBackDownMin = min.add(new Point3D(sideLength, 0, 0));
            Point3D rightBackDownMax = center.add(new Point3D(sideLength, 0, 0));
            OcTree<T> rightBackDown = new OcTree<>(rightBackDownMin, rightBackDownMax, coordinator, this);

            Point3D rightBackUpMin = min.add(new Point3D(sideLength, 0, sideLength));
            Point3D rightBackUpMax = center.add(new Point3D(sideLength, 0, sideLength));
            OcTree<T> rightBackUp = new OcTree<>(rightBackUpMin, rightBackUpMax, coordinator, this);

            Point3D rightFrontDownMin = min.add(new Point3D(sideLength, sideLength, 0));
            Point3D rightFrontDownMax = center.add(new Point3D(sideLength, sideLength, 0));
            OcTree<T> rightFrontDown = new OcTree<>(rightFrontDownMin, rightFrontDownMax, coordinator, this);

            Point3D rightFrontUpMin = min.add(new Point3D(sideLength, sideLength, sideLength));
            Point3D rightFrontUpMax = center.add(new Point3D(sideLength, sideLength, sideLength));
            OcTree<T> rightFrontUp = new OcTree<>(rightFrontUpMin, rightFrontUpMax, coordinator, this);

            for (T item : pointMap.keySet()) {
                Point3D point = pointMap.get(item);
                if (Point3D.inRange(leftBackDownMin, leftBackDownMax, point)) {
                    leftBackDown.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(leftBackUpMin, leftBackUpMax, point)) {
                    leftBackUp.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(leftFrontDownMin, leftFrontDownMax, point)) {
                    leftFrontDown.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(leftFrontUpMin, leftFrontUpMax, point)) {
                    leftFrontUp.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(rightBackDownMin, rightBackDownMax, point)) {
                    rightBackDown.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(rightBackUpMin, rightBackUpMax, point)) {
                    rightBackUp.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(rightFrontDownMin, rightFrontDownMax, point)) {
                    rightFrontDown.add(item);
                    contents.remove(item);
                } else if (Point3D.inRange(rightFrontUpMin, rightFrontUpMax, point)) {
                    rightFrontUp.add(item);
                    contents.remove(item);
                } else {
                    System.out.println("Point not in any sub-octants");
                }
            }

            if (contents.size() > 0) {
                System.out.println("Warning: left over points from subdivision, " + contents.size());
            }

            children.put(OcTreeOctant.LEFTBACKDOWN, leftBackDown);
            children.put(OcTreeOctant.LEFTBACKUP, leftBackUp);
            children.put(OcTreeOctant.LEFTFRONTDOWN, leftFrontDown);
            children.put(OcTreeOctant.LEFTFRONTUP, leftFrontUp);
            children.put(OcTreeOctant.RIGHTBACKDOWN, rightBackDown);
            children.put(OcTreeOctant.RIGHTBACKUP, rightBackUp);
            children.put(OcTreeOctant.RIGHTFRONTDOWN, rightFrontDown);
            children.put(OcTreeOctant.RIGHTFRONTUP, rightFrontUp);
        }
    }

    public int size() {
        int size = contents.size();
        if (children != null) {
            for (OcTreeOctant o : children.keySet()) {
                size += children.get(o).contents.size();
            }
        }
        return size;
    }
}