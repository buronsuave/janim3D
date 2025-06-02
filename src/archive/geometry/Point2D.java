package archive.geometry;

public class Point2D {
    private double x, y;

    public Point2D(double x, double y) {
        set(x, y);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() { return x; }
    public double y() { return y; }

    @Override
    public String toString() {
        return "Point2D(" + x + ", " + y + ")";
    }
}