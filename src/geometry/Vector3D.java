package geometry;

public class Vector3D {
    private double x, y, z;

    public Vector3D(double x, double y, double z) {
        set(x,y,z);
    }
    public Vector3D(Point3D p0, Point3D p1) {
        set(
                p1.x()-p0.x(), p1.y()-p0.y(), p1.z()- p0.z()
        );
    }
    public Vector3D(Vector3D original) {
        set(original.x, original.y(), original.z());
    }
    public void set(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public void normalize() {
        double l = length();
        if (l != 0)
            set(x/l, y/l, z/l);
    }

    public double dot(Vector3D other) {
        return x*other.x + y*other.y + z*other.z;
    }

    public Vector3D opp() {
        return new Vector3D(-x, -y, -z);
    }

    @Override
    public String toString() {
        return "Vector3D<" + x + ", " + y + ", " + z + ">";
    }
}
