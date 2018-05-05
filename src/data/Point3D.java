package data;

public class Point3D {
    private double x;
    private double y;
    private double z;

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(int v) {
        this(v, v, v);
    }

    public Point3D(double v) {
        this(v, v, v);
    }

    public static Point3D zero() {
        return new Point3D(0, 0, 0);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Point3D multiply(double factor) {
        return new Point3D(this.x * factor, this.y * factor, this.z * factor);
    }

    public Point3D divide(double factor) {
        return new Point3D(this.x / factor, this.y / factor, this.z / factor);
    }

    public Point3D add(Point3D other) {
        return new Point3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Point3D subtract(Point3D other) {
        return new Point3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public void set(Point3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public boolean gt(Point3D other) {
        return x > other.x && y > other.y && z > other.z;
    }

    public boolean gte(Point3D other) {
        return x >= other.x && y >= other.y && z >= other.z;
    }

    public boolean lt(Point3D other) {
        return x < other.x && y < other.y && z < other.z;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public static boolean inRange(Point3D min, Point3D max, Point3D test) {
        return test.gte(min) && test.lt(max);
    }

    public static double distance(Point3D a, Point3D b) {
        return Math.sqrt(a.x * b.x + a.y * b.y + a.z * b.z);
    }

    public static double manhattanDistance(Point3D a, Point3D b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z);
    }
}
