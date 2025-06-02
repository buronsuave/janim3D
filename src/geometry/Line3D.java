package geometry;

public class Line3D {
    private Point3D p0, p1;

    public Line3D(double x0, double y0, double z0, double x1, double y1, double z1) {
        this(new Point3D(x0, y0, z0), new Point3D(x1, y1, z1));
    }
    public Line3D(Point3D p0, Point3D p1) {
        this.p0 = p0;
        this.p1 = p1;
    }

    public Point3D getP0() {
        return p0;
    }

    public Point3D getP1() {
        return p1;
    }

    public void setP0(Point3D p0) {
        this.p0 = p0;
    }

    public void setP1(Point3D p1) {
        this.p1 = p1;
    }
}
