package geometry;

import java.awt.Color;

public class Face3D {
    private Point3D[] vertices;
    private Color color;

    public Face3D(Point3D[] vertices, Color color) {
        this.vertices = vertices;
        this.color = color;
    }

    public Vector3D normal() {
        Vector3D u = new Vector3D(vertices[1], vertices[0]);
        Vector3D v = new Vector3D(vertices[3], vertices[0]);
        return u.cross(v);
    }

    public Point3D center() {
        double x = 0, y = 0, z = 0;
        for (Point3D p : vertices) {
            x += p.x(); y += p.y(); z += p.z();
        }
        return new Point3D(x/4, y/4, z/4);
    }

    public Point3D[] getVertices() {
        return vertices;
    }

    public Color getColor() {
        return color;
    }

    public void setVertices(Point3D[] vertices) {
        this.vertices = vertices;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
