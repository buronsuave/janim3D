package projection;

import geometry.Point2D;
import geometry.Point3D;
import geometry.Vector3D;
import math.Transform;

public class ParallelProjection {

    // Project point into the plane with specific normal mapping the center
    public static Point2D project(Point3D point, Vector3D normal, Point3D center) {
        // Copies
        Point3D p = new Point3D(point);
        Vector3D n = new Vector3D(normal);
        n.normalize();

        // Step 1: Move to center
        double dx = center.x();
        double dy = center.y();
        double dz = center.z();
        Transform.translatePoint3D(p, -dx, -dy, -dz);

        // Step 2: Align normal with z axis
        // double semiR = Math.sqrt(n.x()*n.x() + n.y()*n.y());
        double semiR = Math.sqrt(n.x()*n.x() + n.z()*n.z());
        double phi, theta;

        if (n.y() == 0)
            phi = Math.PI/2;
        else {
            phi = Math.atan(semiR / n.y());
            if (phi < 0) phi += Math.PI;
        }

        if (n.z() == 0 && n.x() >= 0)
            theta = Math.PI/2;
        else if (n.z() == 0)
            theta = 3*Math.PI/2;
        else {
            theta = Math.atan(n.x() / n.z());
            if (n.z() < 0) theta += Math.PI;
            else if (theta < 0) theta += 2*Math.PI;
        }

        Transform.rotatePoint3DAroundY(p, -theta);
        Transform.rotatePoint3DAroundX(p, -phi+Math.PI/2);

        // Project p into xy plane
        return new Point2D(p.x(), p.y());
    }
}
