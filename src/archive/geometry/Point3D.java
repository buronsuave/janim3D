package archive.geometry;

public class Point3D {
    private double x, y, z;

    public Point3D(double x, double y, double z) {
        set(x, y, z);
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    @Override
    public String toString() {
        return "Point3D(" + x + ", " + y + ", " + z + ")";
    }
}