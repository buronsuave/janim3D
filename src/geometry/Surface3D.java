package geometry;

import math.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Surface3D extends Shape {

    @Override
    public List<Line3D> getGridLines() {
        int resolution = 40;
        double step = 2*Math.PI/resolution;

        List<Point3D> initialRing = new ArrayList<>();
        List<Point3D> ring = new ArrayList<>();
        List<Point3D> nextRing = new ArrayList<>();
        List<Line3D> lines = new ArrayList<>();
        double t1 = 0;
        double t2 = 0;

        // Compute first ring
        for (int j = 0; j < resolution; ++j) {
            Point3D p = new Point3D(
                    (10 + 3*Math.sin(t2)),
                    0, // t1 = 0
                    3*Math.cos(t2)
            );
            Transform.apply(getTransformation(), p);
            ring.add(p);
            initialRing.add(p);
            t2 += step;

            // Add line
            if (j == 0) continue;
            lines.add(new Line3D(
                    ring.get(j), ring.get(j-1)
            ));
        }
        // Add last line of first ring
        lines.add(new Line3D(
                ring.get(resolution-1), ring.get(0)
        ));

        for (int i = 1; i < resolution; ++i) {
            t2 = 0;
            nextRing.clear();

            for (int j = 0; j < resolution; ++j) {
                Point3D p = new Point3D(
                        (10 + 3*Math.sin(t2)) * Math.cos(t1),
                        (10 + 3*Math.sin(t2)) * Math.sin(t1),
                        3*Math.cos(t2)
                );
                Transform.apply(getTransformation(), p);
                nextRing.add(p);

                t2 += step;

                // Add line
                if (j == 0) continue;
                lines.add(new Line3D(
                        nextRing.get(j), nextRing.get(j-1)
                ));
            }

            // Add last line of next ring
            lines.add(new Line3D(
                    nextRing.get(resolution-1), nextRing.get(0)
            ));
            t1 += step;

            // Add lines of next ring and previous ring
            for (int k = 0; k < resolution; ++k) {
                lines.add(new Line3D(
                    nextRing.get(k), ring.get(k)
                ));
            }

            ring.clear();
            ring.addAll(nextRing);
        }

        // Bind last ring and first ring
        for (int k = 0; k < resolution; ++k) {
            lines.add(new Line3D(
                    initialRing.get(k), nextRing.get(k)
            ));
        }

        return lines;
    }

    @Override
    public String toString() {
        Point3D center = getCenter();
        return "Surface3D("+ center.x() + "," + center.y() + "," + center.z() +
                ", transformation = " + Arrays.deepToString(getTransformation()) + ")";
    }
}
