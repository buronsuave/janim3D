package geometry;

import math.Transform;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class Cube extends Shape implements SolidShape {
    private final double size;

    public Cube(double size) {
        super();
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

    @Override
    public List<Face3D> getFaces() {
        Point3D[] v = new Point3D[] {
                new Point3D(size/2, size/2, size/2),
                new Point3D(size/2, size/2, -size/2),
                new Point3D(-size/2, size/2, -size/2),
                new Point3D(-size/2, size/2, size/2),
                new Point3D(size/2, -size/2, size/2),
                new Point3D(size/2, -size/2, -size/2),
                new Point3D(-size/2, -size/2, -size/2),
                new Point3D(-size/2, -size/2, size/2)
        };

        for (Point3D vertex : v) {
            Transform.apply(getTransformation(), vertex);
        }

        Color[] colors = new Color[] {
                new Color(214,212,200,255),
                new Color(192,160,25,255),
                new Color(182,36,36,255),
                new Color(209,91,30,255),
                new Color(9,147,209,255),
                new Color(29,146,68,255)

        };

        Face3D[] faces = new Face3D[] {
                // Top (+Y) - CCW when viewed from above
                new Face3D(new Point3D[] {v[0], v[3], v[2], v[1]}, colors[0]),

                // Bottom (-Y) - CCW when viewed from below
                new Face3D(new Point3D[] {v[4], v[5], v[6], v[7]}, colors[1]),

                // Right (+X) - CCW when viewed from +X
                new Face3D(new Point3D[] {v[0], v[1], v[5], v[4]}, colors[2]),

                // Left (-X) - CCW when viewed from -X
                new Face3D(new Point3D[] {v[3], v[7], v[6], v[2]}, colors[3]),

                // Front (+Z) - CCW when viewed from +Z
                new Face3D(new Point3D[] {v[0], v[4], v[7], v[3]}, colors[4]),

                // Back (-Z) - CCW when viewed from -Z
                new Face3D(new Point3D[] {v[1], v[2], v[6], v[5]}, colors[5])
        };


        return Arrays.asList(faces);
    }
}
