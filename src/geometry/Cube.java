package geometry;

import math.Transform;

import java.util.Arrays;
import java.util.List;

public class Cube extends Shape {
    private double size;

    public Cube(double size) {
        super();
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public List<Line3D> getGridLines() {
        Point3D[] vertices = new Point3D[] {
                new Point3D(size/2, size/2, size/2),
                new Point3D(size/2, size/2, -size/2),
                new Point3D(-size/2, size/2, -size/2),
                new Point3D(-size/2, size/2, size/2),
                new Point3D(size/2, -size/2, size/2),
                new Point3D(size/2, -size/2, -size/2),
                new Point3D(-size/2, -size/2, -size/2),
                new Point3D(-size/2, -size/2, size/2)
        };

        for (Point3D vertex : vertices) {
            Transform.apply(getTransformation(), vertex);
        }

        Line3D[] edges = new Line3D[] {
                new Line3D(vertices[0], vertices[1]),
                new Line3D(vertices[1], vertices[2]),
                new Line3D(vertices[2], vertices[3]),
                new Line3D(vertices[3], vertices[0]),
                new Line3D(vertices[0], vertices[4]),
                new Line3D(vertices[1], vertices[5]),
                new Line3D(vertices[2], vertices[6]),
                new Line3D(vertices[3], vertices[7]),
                new Line3D(vertices[4], vertices[5]),
                new Line3D(vertices[5], vertices[6]),
                new Line3D(vertices[6], vertices[7]),
                new Line3D(vertices[7], vertices[4])
        };

        return Arrays.stream(edges).toList();
    }

    @Override
    public String toString() {
        Point3D center = getCenter();
        return "Cube("+ center.x() + "," + center.y() + "," + center.z() +
                ", transformation = " + Arrays.deepToString(getTransformation()) + ")";
    }
}
