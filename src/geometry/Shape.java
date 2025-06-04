package geometry;

import math.Matrix;
import math.Transform;
import java.util.List;

public abstract class Shape {
    private double[][] transformation;

    public Shape() {
        resetTransformation();
    }

    public void resetTransformation() {
        transformation = new double[][] {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
    }
    public double[][] getTransformation() {
        return transformation;
    }
    public void setTransformation(double[][] transformation) {
        this.transformation = transformation;
    }

    public  abstract List<Line3D> getGridLines();

    public void rotate(double t, char axis) {
        double[][] rotationOnly = {
                { transformation[0][0], transformation[0][1], transformation[0][2], 0 },
                { transformation[1][0], transformation[1][1], transformation[1][2], 0 },
                { transformation[2][0], transformation[2][1], transformation[2][2], 0 },
                { 0, 0, 0, 1 }
        };

        double[][] rotation = switch (axis) {
            case 'x' -> Matrix.rotationXMatrix(t);
            case 'y' -> Matrix.rotationYMatrix(t);
            case 'z' -> Matrix.rotationZMatrix(t);
            default -> throw new IllegalArgumentException("Invalid axis");
        };

        double[][] temp = Matrix.mul(rotation, rotationOnly);
        assert temp != null;

        // Copy translation
        temp[0][3] = transformation[0][3];
        temp[1][3] = transformation[1][3];
        temp[2][3] = transformation[2][3];

        transformation = temp;
    }

    public void translate(double dx, double dy, double dz) {
        transformation = Matrix.mul(
                Matrix.translationMatrix(dx, dy, dz),
                transformation
        );
    }

    public void scale(double sx, double sy, double sz) {
        transformation = Matrix.mul(
                transformation,
                Matrix.scaleMatrix(sx, sy, sz)
        );
    }

    public Point3D getCenter() {
        Point3D center = new Point3D(0, 0, 0);
        Transform.apply(transformation, center);
        return center;
    }
}
