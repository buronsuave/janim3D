package archive.geometry;

import archive.math.Matrix;

public class Cube {
    private Point3D center;
    private double size;
    private double[][] transformation;

    public Cube() { this(new Point3D(0,0,0), 2); }
    public Cube(Point3D center, double size) {
        this.center = center;
        this.size = size;
        transformation = new double[][] {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
    }

    public Point3D getCenter() { return center; }
    public double getSize() { return size; }
    public double[][] getTransformation() { return transformation; }

    public void setCenter(Point3D center) { this.center = center; }
    public void setSize(double size) { this.size = size; }
    public void setTransformation(double[][] transformation) { this.transformation = transformation; }

    public void resetTransformation() {
        transformation = new double[][] {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
    }

    public void scale(double sx, double sy, double sz) {
        transformation = Matrix.mul(transformation, Matrix.scaleM(sx, sy, sz));
        center = Matrix.applyToPoint3D(transformation, center);
    }

    public void rotate(Vector3D axis, double t) {
        // During internal rotation, center remains the same
        transformation = Matrix.mul(transformation, Matrix.rotationM(axis, t));
    }

    public void rotateAroundPoint(Point3D center, Vector3D axis, double t) {
        transformation = Matrix.mul(transformation, Matrix.rotationAroundPointM(center, axis, t));
        center = Matrix.applyToPoint3D(transformation, center);
    }

    public Point3D[] getVertices() {
        Point3D[] vertices = new Point3D[8];
        vertices[0] = new Point3D(center.x() + size/2, center.y() + size/2, center.z() + size/2);
        vertices[1] = new Point3D(center.x() - size/2, center.y() + size/2, center.z() + size/2);
        vertices[2] = new Point3D(center.x() - size/2, center.y() - size/2, center.z() + size/2);
        vertices[3] = new Point3D(center.x() + size/2, center.y() - size/2, center.z() + size/2);
        vertices[4] = new Point3D(center.x() + size/2, center.y() + size/2, center.z() - size/2);
        vertices[5] = new Point3D(center.x() - size/2, center.y() + size/2, center.z() - size/2);
        vertices[6] = new Point3D(center.x() - size/2, center.y() - size/2, center.z() - size/2);
        vertices[7] = new Point3D(center.x() + size/2, center.y() - size/2, center.z() - size/2);

        for (Point3D vertex : vertices) {
            //System.out.println("Vertex coordinates before transformation: " + vertex);
            vertex = Matrix.applyToPoint3D(transformation, vertex);
            //System.out.println("Vertex coordinates after transformation: " + vertex);
        }

        return vertices;
    }
}
