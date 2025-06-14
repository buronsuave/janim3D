package geometry;

import math.Transform;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Surface3D extends Shape implements SolidShape {
    private double mainR = 10;
    private double tubeR = 3;
    private Color color = new Color(9,147,209,255);

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
                    (mainR + tubeR*Math.sin(t2)),
                    0, // t1 = 0
                    tubeR*Math.cos(t2)
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
                        (mainR + tubeR*Math.sin(t2)) * Math.cos(t1),
                        (mainR + tubeR*Math.sin(t2)) * Math.sin(t1),
                        tubeR*Math.cos(t2)
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

    public void setColor(Color color) {
        this.color = color;
    }

    public void setMainR(double mainR) {
        this.mainR = mainR;
    }

    public void setTubeR(double tubeR) {
        this.tubeR = tubeR;
    }

    @Override
    public List<Face3D> getFaces() {
        int resolution = 40;
        double step = 2 * Math.PI / resolution;

        List<Face3D> faces = new ArrayList<>();
        Point3D[][] grid = new Point3D[resolution][resolution];

        // Generate the grid points
        for (int i = 0; i < resolution; ++i) {
            double t1 = i * step;
            for (int j = 0; j < resolution; ++j) {
                double t2 = j * step;

                Point3D p = new Point3D(
                        (mainR + tubeR * Math.sin(t2)) * Math.cos(t1),
                        (mainR + tubeR * Math.sin(t2)) * Math.sin(t1),
                        tubeR * Math.cos(t2)
                );
                Transform.apply(getTransformation(), p);
                grid[i][j] = p;
            }
        }

        // Create faces from quads
        for (int i = 0; i < resolution; ++i) {
            for (int j = 0; j < resolution; ++j) {
                Point3D a = grid[i][j];
                Point3D b = grid[i][(j + 1) % resolution];
                Point3D c = grid[(i + 1) % resolution][(j + 1) % resolution];
                Point3D d = grid[(i + 1) % resolution][j];
                faces.add(new Face3D(new Point3D[]{a, b, c, d}, color));
            }
        }

        return faces;
    }

    @Override
    public String toString() {
        Point3D center = getCenter();
        return "Surface3D("+ center.x() + "," + center.y() + "," + center.z() +
                ", transformation = " + Arrays.deepToString(getTransformation()) + ")";
    }
}