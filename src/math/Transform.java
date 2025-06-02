package math;

import geometry.Point2D;
import geometry.Point3D;
import geometry.Vector3D;

public class Transform {

    public static void apply(double[][] m, Point3D p) {
        double[][] newPoint = Matrix.mul(
                m,
                new double[][] {
                        {p.x()},
                        {p.y()},
                        {p.z()},
                        {1}
                }
        );

        assert newPoint != null;
        p.set(
                newPoint[0][0], newPoint[1][0], newPoint[2][0]
        );
    }

    public static void toRealPoint2D(Point2D p, int s, int w, int h) {
        double[][] realPoint = Matrix.mul(
                new double[][] {
                        {s,  0, w},
                        {0, -s, h},
                        {0,  0, 1}
                },
                new double[][] {
                        {p.x()},
                        {p.y()},
                        {1}
                }
        );

        assert realPoint != null;
        p.set(
                realPoint[0][0], realPoint[1][0]
        );
    }

    public static void rotateVector3DAroundX(Vector3D v, double t) {
        double[][] newVector = Matrix.mul(
                Matrix.rotationXMatrix(t),
                new double[][] {
                        {v.x()},
                        {v.y()},
                        {v.z()},
                        {1}
                }
        );

        assert newVector != null;
        v.set(
                newVector[0][0], newVector[1][0], newVector[2][0]
        );
    }

    public static void rotateVector3DAroundY(Vector3D v, double t) {
        double[][] newVector = Matrix.mul(
                Matrix.rotationYMatrix(t),
                new double[][] {
                        {v.x()},
                        {v.y()},
                        {v.z()},
                        {1}
                }
        );

        assert newVector != null;
        v.set(
                newVector[0][0], newVector[1][0], newVector[2][0]
        );
    }

    public static void rotateVector3DAroundZ(Vector3D v, double t) {
        double[][] newVector = Matrix.mul(
                Matrix.rotationZMatrix(t),
                new double[][] {
                        {v.x()},
                        {v.y()},
                        {v.z()},
                        {1}
                }
        );

        assert newVector != null;
        v.set(
                newVector[0][0], newVector[1][0], newVector[2][0]
        );
    }

    public static void rotatePoint3DAroundX(Point3D p, double t) {
        Vector3D pointVector = new Vector3D(p.x(), p.y(), p.z());
        rotateVector3DAroundX(pointVector, t);
        p.set(
                pointVector.x(), pointVector.y(), pointVector.z()
        );
    }

    public static void rotatePoint3DAroundY(Point3D p, double t) {
        Vector3D pointVector = new Vector3D(p.x(), p.y(), p.z());
        rotateVector3DAroundY(pointVector, t);
        p.set(
                pointVector.x(), pointVector.y(), pointVector.z()
        );
    }

    public static void rotatePoint3DAroundZ(Point3D p, double t) {
        Vector3D pointVector = new Vector3D(p.x(), p.y(), p.z());
        rotateVector3DAroundZ(pointVector, t);
        p.set(
                pointVector.x(), pointVector.y(), pointVector.z()
        );
    }

    public static void translatePoint3D(Point3D p, double dx, double dy, double dz) {
        double[][] newPoint = Matrix.mul(
                new double[][] {
                        {1, 0, 0, dx},
                        {0, 1, 0, dy},
                        {0, 0, 1, dz},
                        {0, 0, 0, 1 }
                },
                new double[][] {
                        {p.x()},
                        {p.y()},
                        {p .z()},
                        {1}
                }
        );

        assert newPoint != null;
        p.set(
                newPoint[0][0], newPoint[1][0], newPoint[2][0]
        );
    }
}
