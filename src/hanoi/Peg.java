package hanoi;

import geometry.Face3D;
import geometry.Line3D;
import geometry.Point3D;
import geometry.Shape;
import geometry.SolidShape;
import math.Transform;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Peg extends Shape implements SolidShape {
    private final double radius = 3.0;
    private final double height = 30.0;

    @Override
    public List<Face3D> getFaces() {
        int resolution = 40;
        double stepAngle = 2 * Math.PI / resolution;
        double stepHeight = height / resolution;

        List<Face3D> faces = new ArrayList<>();
        Point3D[][] grid = new Point3D[resolution][resolution];

        // Generate cylinder surface points
        for (int i = 0; i < resolution; ++i) {
            double y = i * stepHeight;
            for (int j = 0; j < resolution; ++j) {
                double theta = j * stepAngle;
                Point3D p = new Point3D(
                        radius * Math.cos(theta),
                        y,
                        -radius * Math.sin(theta)
                );
                Transform.apply(getTransformation(), p);
                grid[i][j] = p;
            }
        }

        // Lateral surface faces (quads)
        for (int i = 0; i < resolution - 1; ++i) {
            for (int j = 0; j < resolution; ++j) {
                Point3D a = grid[i][j];
                Point3D b = grid[i][(j + 1) % resolution];
                Point3D c = grid[i + 1][(j + 1) % resolution];
                Point3D d = grid[i + 1][j];
                faces.add(new Face3D(new Point3D[]{a, b, c, d}, Color.LIGHT_GRAY));
            }
        }

        // Cap centers
        Point3D centerBottom = new Point3D(0, 0, 0);
        Point3D centerTop = new Point3D(0, height, 0);
        Transform.apply(getTransformation(), centerBottom);
        Transform.apply(getTransformation(), centerTop);

        // Bottom cap (y = 0)
        for (int j = 0; j < resolution; ++j) {
            Point3D a = grid[0][j];
            Point3D b = grid[0][(j + 1) % resolution];
            faces.add(new Face3D(new Point3D[]{b, a, centerBottom, centerBottom}, Color.LIGHT_GRAY));
        }

        // Top cap (y = height)
        for (int j = 0; j < resolution; ++j) {
            Point3D a = grid[resolution - 1][j];
            Point3D b = grid[resolution - 1][(j + 1) % resolution];
            faces.add(new Face3D(new Point3D[]{a, b, centerTop, centerTop}, Color.LIGHT_GRAY));
        }

        return faces;
    }

    @Override
    public List<Line3D> getGridLines() {
        int resolution = 40;
        double stepAngle = 2*Math.PI/resolution;
        double stepVertical = height/resolution;

        List<Point3D> ring = new ArrayList<>();
        List<Point3D> nextRing = new ArrayList<>();
        List<Line3D> lines = new ArrayList<>();
        double t1 = 0;
        double t2 = 0;

        // Compute first ring
        for (int j = 0; j < resolution; ++j) {
            Point3D p = new Point3D(
                    radius * Math.cos(t2),
                    0, // t1 = 0
                    radius * Math.sin(t2)
            );
            Transform.apply(getTransformation(), p);
            ring.add(p);
            t2 += stepAngle;

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

        t1 += stepVertical;
        for (int i = 1; i < resolution; ++i) {
            t2 = 0;
            nextRing.clear();

            for (int j = 0; j < resolution; ++j) {
                Point3D p = new Point3D(
                        radius*Math.cos(t2),
                        t1,
                        radius*Math.sin(t2)
                );
                Transform.apply(getTransformation(), p);
                nextRing.add(p);

                t2 += stepAngle;

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

            t1 += stepVertical;

            // Add lines of next ring and previous ring
            for (int k = 0; k < resolution; ++k) {
                lines.add(new Line3D(
                        nextRing.get(k), ring.get(k)
                ));
            }

            ring.clear();
            ring.addAll(nextRing);
        }

        return lines;
    }
}