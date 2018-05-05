package data;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class OcTree<T> {
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
    private int remainingDepth;
    private Function<T, Point3D> cordMapper;
    private Collection<T> contents;
    private OcTree<T> parent;
    private Map<OcTreeOctant, OcTree<T>> children;

    public OcTree(Point3D min, Point3D max, Function<T, Point3D> cordMapper, int depth) {
        this(min, max, cordMapper, null, depth);
    }

    private OcTree(Point3D min, Point3D max, Function<T, Point3D> cordMapper, OcTree<T> parent, int remainingDepth) {
        this.min = min;
        this.max = max;
        this.parent = parent;
        this.cordMapper = cordMapper;
        this.contents = new ArrayList<>();
        this.remainingDepth = remainingDepth;
        if (remainingDepth != 0) {
            buildSubtrees();
        }
    }

    public OcTree<T> getChild(OcTreeOctant o) {
        return children.get(o);
    }

    public OcTreeOctant getDirectRelationship(OcTree<T> tree) {
        if (children.containsValue(tree)) {
            for (OcTreeOctant octant : children.keySet()) {
                if (getChild(octant) == tree) {
                    return octant;
                }
            }
        }
        return null;
    }

    public OcTree<T> getParent() {
        return parent;
    }

    public OcTree<T> getOctantBy(Point3D point) {
        if (children != null) {
            for (OcTreeOctant octant : children.keySet()) {
                OcTree<T> tree = children.get(octant);
                if (Point3D.inRange(tree.min, tree.max, point)) {
                    return tree.getOctantBy(point);
                }
            }
        }
        return this;
    }

    /**
     * @return the 26 adjacent nodes (including diagonals) to this one, if available.
     */
    public Collection<OcTree<T>> getAdjacentLeaves() {
        Set<OcTree<T>> adjacentNodes = new HashSet<>();
        if (parent != null) {
            // Get all the nodes that share a parent with this one that aren't this one
            for (OcTreeOctant octant : OcTreeOctant.values()) {
                adjacentNodes.add(parent.getChild(octant));
            }
            adjacentNodes.remove(this);

            if (parent.getParent() != null) {
                switch (parent.getDirectRelationship(this)) {
                    case LEFTBACKUP:
                        break;
                    case LEFTFRONTUP:

                        break;
                    case RIGHTBACKUP:

                        break;
                    case LEFTBACKDOWN:

                        break;
                    case RIGHTFRONTUP:

                        break;
                    case LEFTFRONTDOWN:

                        break;
                    case RIGHTBACKDOWN:

                        break;
                    case RIGHTFRONTDOWN:

                        break;
                    default:
                        break;
                }
            }
        }

        return adjacentNodes;
    }

    public void add(T obj) {
        if (remainingDepth == 0) {
            contents.add(obj);
        } else {
            if (children == null) {
                contents.add(obj);
                Map<T, Point3D> pointMap = new HashMap<>();
                contents.forEach(item -> pointMap.put(item, cordMapper.apply(item)));

                buildSubtrees();

                while (!contents.isEmpty()) {
                    T item = contents.iterator().next();
                    Point3D point = pointMap.get(item);

                    for (OcTreeOctant octant : children.keySet()) {
                        OcTree<T> tree = children.get(octant);
                        if (Point3D.inRange(tree.min, tree.max, point)) {
                            tree.add(item);
                            contents.remove(item);
                            break;
                        }
                    }
                }

                if (contents.size() > 0) {
                    System.out.println("Warning: left over points from subdivision, " + contents.size());
                }
            } else {
                for (OcTreeOctant o : children.keySet()) {
                    Point3D point = cordMapper.apply(obj);
                    if (Point3D.inRange(children.get(o).min, children.get(o).max, point)) {
                        children.get(o).add(obj);
                        break;
                    }
                }
            }
        }
    }

    public void clear() {
        contents.clear();
        if (children != null) {
            forEachChild(OcTree::clear);
        }
    }

    private void buildSubtrees() {
        children = new HashMap<>();

        double sideLength = max.subtract(min).divide(2).getX();
        Point3D center = min.add(max.subtract(min).divide(2));

        OcTree<T> leftBackDown = new OcTree<>(min, center, cordMapper, this, remainingDepth - 1);

        OcTree<T> leftBackUp = new OcTree<>(
                min.add(new Point3D(0, 0, sideLength)),
                center.add(new Point3D(0, 0, sideLength)), cordMapper, this, remainingDepth - 1);

        OcTree<T> leftFrontDown = new OcTree<>(
                min.add(new Point3D(0, sideLength, 0)),
                center.add(new Point3D(0, sideLength, 0)), cordMapper, this, remainingDepth - 1);

        OcTree<T> leftFrontUp = new OcTree<>(
                min.add(new Point3D(0, sideLength, sideLength)),
                center.add(new Point3D(0, sideLength, sideLength)), cordMapper, this, remainingDepth - 1);

        OcTree<T> rightBackDown = new OcTree<>(
                min.add(new Point3D(sideLength, 0, 0)),
                center.add(new Point3D(sideLength, 0, 0)), cordMapper, this, remainingDepth - 1);

        OcTree<T> rightBackUp = new OcTree<>(
                min.add(new Point3D(sideLength, 0, sideLength)),
                center.add(new Point3D(sideLength, 0, sideLength)), cordMapper, this, remainingDepth - 1);

        OcTree<T> rightFrontDown = new OcTree<>(
                min.add(new Point3D(sideLength, sideLength, 0)),
                center.add(new Point3D(sideLength, sideLength, 0)), cordMapper, this, remainingDepth - 1);

        OcTree<T> rightFrontUp = new OcTree<>(
                min.add(new Point3D(sideLength, sideLength, sideLength)),
                center.add(new Point3D(sideLength, sideLength, sideLength)), cordMapper, this, remainingDepth - 1);

        children.put(OcTreeOctant.LEFTBACKDOWN, leftBackDown);
        children.put(OcTreeOctant.LEFTBACKUP, leftBackUp);
        children.put(OcTreeOctant.LEFTFRONTDOWN, leftFrontDown);
        children.put(OcTreeOctant.LEFTFRONTUP, leftFrontUp);
        children.put(OcTreeOctant.RIGHTBACKDOWN, rightBackDown);
        children.put(OcTreeOctant.RIGHTBACKUP, rightBackUp);
        children.put(OcTreeOctant.RIGHTFRONTDOWN, rightFrontDown);
        children.put(OcTreeOctant.RIGHTFRONTUP, rightFrontUp);
    }

    public int size() {
        int size = contents.size();
        if (children != null) {
            for (OcTreeOctant o : children.keySet()) {
                size += children.get(o).size();
            }
        }
        return size;
    }

    public Collection<T> getContents() {
        return contents;
    }

    public Function<T, Point3D> getCordMapper() {
        return cordMapper;
    }

    // Returns [minimum number in a leaf node, maximum number in a leaf node, total number of leaf nodes]
    public int[] getStatistics(int[] statsContainer) {
        if (children == null) {
            if (statsContainer[0] > contents.size()) {
                statsContainer[0] = contents.size();
            }

            if (statsContainer[1] < contents.size()) {
                statsContainer[1] = contents.size();
            }

            statsContainer[2]++;
        } else {
            forEachChild(child -> child.getStatistics(statsContainer));
        }

        return statsContainer;
    }

    public Collection<T> collectValues() {
        Collection<T> items = new ArrayList<>();
        if (children != null) {
            forEachChild(child -> items.addAll(child.collectValues()));
        } else {
            if (contents.size() > 0) {
                items.addAll(contents);
            }
        }
        return items;
    }

    public void forEachChild(Consumer<OcTree<T>> c) {
        for (OcTreeOctant octant : children.keySet()) {
            c.accept(children.get(octant));
        }
    }

    public void printStats() {
        int[] stats = {0, 0, 0};
        getStatistics(stats);
        System.out.println("Minimum number in leaf node: " + stats[0]);
        System.out.println("Maximum number in leaf node: " + stats[1]);
        System.out.println("Number of leaf nodes: " + stats[2]);
        System.out.println("Average colors per leaf: " + ((double) size()) / ((double) stats[2]));
        System.out.println("Total items: " + size());
    }

    @Override
    public String toString() {
        return "min: " + min + ", max: " + max;
    }
}