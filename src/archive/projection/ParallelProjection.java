package archive.projection;

import archive.geometry.Point2D;
import archive.geometry.Point3D;
import archive.geometry.Vector3D;

public class ParallelProjection {

    public static Point2D project(Point3D point, Vector3D normal) {
        // Step 1: Normalize the normal vector (in place)
        normal.normalize();

        // Step 2: Compute auxiliary orthonormal basis (u, v) on the plane
        Vector3D u;
        if (Math.abs(normal.x()) < 1e-6 && Math.abs(normal.y()) < 1e-6) {
            // If normal is aligned with Z axis, pick X axis as reference
            u = new Vector3D(1, 0, 0);
        } else {
            // Choose a vector not collinear with normal to build u
            u = new Vector3D(-normal.y(), normal.x(), 0);
        }

        u.normalize();

        // Compute v = n × u
        Vector3D v = normal.cross(u);  // v is orthogonal to both n and u
        v.normalize();

        // Step 3: Project the 3D point into the plane using dot products
        double x2d = point.x() * u.x() + point.y() * u.y() + point.z() * u.z();
        double y2d = point.x() * v.x() + point.y() * v.y() + point.z() * v.z();

        return new Point2D(x2d, y2d);
    }
}