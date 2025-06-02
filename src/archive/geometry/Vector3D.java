package archive.geometry;

public class Vector3D {
    private double x, y, z;

    public Vector3D(double x, double y, double z) {
        set(x, y, z);
    }

    public Vector3D(Point3D p0, Point3D p1) {
        set(
                p1.x() - p0.x(),
                p1.y() - p0.y(),
                p1.z() - p0.z()
        );
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public void normalize() {
        double mag = magnitude();
        if (mag != 0)
            set(x/mag, y/mag, z/mag);
    }

    public double dot(Vector3D other) {
        return x*other.x + y*other.y + z*other.z;
    }

    public Vector3D cross(Vector3D other) {
        return new Vector3D(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    public double magnitude() {
        return x*x + y*y + z*z;
    }

    @Override
    public String toString() {
        return "Vector3D<" + x + ", " + y + ", " + z + ">";
    }
}