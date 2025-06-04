package geometry;

import math.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GaussSurface3D extends Shape {
    @Override
    public List<Line3D> getGridLines() {
        int resolution = 75;
        double xStep = (double) 6 /resolution;
        double zStep = (double) 6 /resolution;

        List<Point3D> layer = new ArrayList<>();
        List<Point3D> nextLayer = new ArrayList<>();
        List<Line3D> lines = new ArrayList<>();
        double x = -3;
        double z = -3;

        // Compute first layer
        for (int j = 0; j < resolution; ++j) {
            Point3D p = new Point3D(
                8*x, 8*Math.exp(-(x*x + z*z)), 8*z
            );
            Transform.apply(getTransformation(), p);
            layer.add(p);
            x += xStep;

            // Add line
            if (j == 0) continue;
            lines.add(new Line3D(
                    layer.get(j), layer.get(j-1)
            ));
        }

        z += zStep;
        for (int i = 1; i < resolution; ++i) {
            x = -3;
            nextLayer.clear();

            for (int j = 0; j < resolution; ++j) {
                Point3D p = new Point3D(
                        8*x, 8*Math.exp(-(x*x + z*z)), 8*z
                );
                Transform.apply(getTransformation(), p);
                nextLayer.add(p);

                x += xStep;

                // Add line
                if (j == 0) continue;
                lines.add(new Line3D(
                        nextLayer.get(j), nextLayer.get(j-1)
                ));
            }

            z += zStep;

            // Add lines of next layer and previous layer
            for (int k = 0; k < resolution; ++k) {
                lines.add(new Line3D(
                        nextLayer.get(k), layer.get(k)
                ));
            }

            layer.clear();
            layer.addAll(nextLayer);
        }

        return lines;
    }

    @Override
    public String toString() {
        Point3D center = getCenter();
        return "GaussSurface("+ center.x() + "," + center.y() + "," + center.z() +
                ", transformation = " + Arrays.deepToString(getTransformation()) + ")";
    }
}
