package math;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Matrix {
    public static double[][] scaleMatrix(double sx, double sy, double sz) {
        return new double[][] {
                {sx, 0,  0,  0},
                {0,  sy, 0,  0},
                {0,  0,  sz, 0},
                {0,  0,  0,  1}
        };
    }

    public static double[][] translationMatrix(double dx, double dy, double dz) {
        return new double[][] {
                {1, 0, 0, dx},
                {0, 1, 0, dy},
                {0, 0, 1, dz},
                {0, 0, 0, 1 }
        };
    }

    public static double[][] rotationXMatrix(double t) {
        return new double[][] {
                {1, 0,      0,       0},
                {0, cos(t), -sin(t), 0},
                {0, sin(t), cos(t),  0},
                {0, 0,      0,       1}
        };
    }

    public static double[][] rotationYMatrix(double t) {
        return new double[][] {
                {cos(t),  0, sin(t), 0},
                {0,       1, 0,       0},
                {-sin(t), 0, cos(t),  0},
                {0,       0, 0,       1}
        };
    }

    public static double[][] rotationZMatrix(double t) {
        return new double[][] {
                {cos(t), -sin(t), 0, 0},
                {sin(t),  cos(t), 0, 0},
                {0     ,  0     , 1, 0},
                {0     ,  0     , 0, 1},
        };
    }

    public static double[][] mul(double[][] a, double[][] b) {
        if (a.length == 0 || b.length == 0) return null;
        if (a[0].length != b.length) return null;

        // Perform multiplication
        double[][] c = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; ++i) {
            for (int j = 0; j < b[0].length; ++j) {
                double s = 0;
                for (int k = 0; k < a[0].length; ++k) {
                    s += (a[i][k] * b[k][j]);
                }
                c[i][j] = s;
            }
        }

        return c;
    }
}
