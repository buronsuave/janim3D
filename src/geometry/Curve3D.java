package geometry;

import math.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Curve3D extends Shape {
    @Override
    public List<Line3D> getGridLines() {
        int resolution = 400;
        Point3D[] vertices = new Point3D[resolution];

        double x = -5;
        for (int i = 0; i < resolution; ++i) {
            vertices[i] = new Point3D(2*x, 2*Math.sin(2*x), 2*(x+Math.cos(2*x)));
            x += (double) 10 / resolution;
        }

        for (Point3D vertex : vertices) {
            Transform.apply(getTransformation(), vertex);
        }

        List<Line3D> lines = new ArrayList<>();
        for (int j = 0; j < vertices.length-1; j++) {
            lines.add(new Line3D(vertices[j], vertices[j+1]));
        }

        return lines;
    }

    @Override
    public String toString() {
        Point3D center = getCenter();
        return "Curve3D("+ center.x() + "," + center.y() + "," + center.z() +
                ", transformation = " + Arrays.deepToString(getTransformation()) + ")";
    }
}
