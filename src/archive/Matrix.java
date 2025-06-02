package archive.math;

import archive.geometry.Point3D;
import archive.geometry.Vector3D;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Matrix {
    public static Point3D applyToPoint3D(double[][] m, Point3D p) {
        double[][] ph = new double[][] { {p.x()}, {p.y()}, {p.z()}, {1} };
        double[][] newPh = mul(m, ph);
        assert newPh != null;
        return new Point3D(newPh[0][0], newPh[1][0], newPh[2][0]);
    }

    public static Vector3D applyToVector3D(double[][] m, Vector3D v) {
        double[][] vh = new double[][] { {v.x()}, {v.y()}, {v.z()}, {1} };
        double[][] newVh = mul(m, vh);
        assert newVh != null;
        return new Vector3D(newVh[0][0], newVh[1][0], newVh[2][0]);
    }

    public static double[][] rotationAroundPointM(Point3D center, Vector3D axis, double t) {
        double dx = center.x();
        double dy = center.y();
        double dz = center.z();

        return mul(translationM(-dx,-dy,-dz), mul(rotationM(axis,t), translationM(dx,dy,dz)));
    }

    public static double[][] scaleM(double sx, double sy, double sz) {
        return new double[][] {
                {sx, 0,  0,  0},
                {0,  sy, 0,  0},
                {0,  0,  sz, 0},
                {0,  0,  0,  1}
        };
    }

    public static double[][] translationM(double dx, double dy, double dz) {
        return new double[][] {
                {1, 0, 0, dx},
                {0, 1, 0, dy},
                {0, 0, 1, dz},
                {0, 0, 0, 1}
        };
    }

    public static double[][] rotationM(Vector3D axis, double t) {
        Vector3D u = new Vector3D(axis.x(), axis.y(), axis.z());
        u.normalize();

        double ux = u.x();
        double uy = u.y();
        double uz = u.z();
        double cos2 = 1-cos(t);

        return new double[][] {
                {ux*ux*cos2 + cos(t),       ux*uy*cos2 - uz*sin(t),     ux*uz*cos2 + uy*sin(t),     0},
                {ux*uy*cos2 + uz*sin(t),    uy*uy*cos2 + cos(t),        uy*uz*cos2 - ux*sin(t),     0},
                {ux*uz*cos2 - uy*sin(t),    ux*uy*cos2 + ux*sin(t),     uz*uz*cos2 + cos(t),        0},
                {0,                         0,                          0,                          1}
        };
    }

    public static double[][] rotationXM(double t) {
        return rotationM(new Vector3D(1, 0, 0), t);
    }

    public static double[][] rotationYM(double t) {
        return rotationM(new Vector3D(0, 1, 0), t);
    }

    public static double[][] rotationZM(double t) {
        return rotationM(new Vector3D(0, 0, 1), t);
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
