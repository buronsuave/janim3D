package graphics;

import geometry.Face3D;
import geometry.Line3D;
import geometry.Point2D;
import geometry.Point3D;
import geometry.Shape;
import geometry.SolidShape;
import geometry.Vector3D;
import math.Transform;
import projection.ParallelProjection;
import projection.PerspectiveProjection;
import projection.Projection;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class Janim3D {
    float[][] zBuffer;
    private int width;
    private int height;
    private int scale;
    private Color color = Color.WHITE; // Default color value
    private BufferedImage buffer;
    private Vector3D camDirection;
    private Point3D camPosition;
    private Projection projection;
    private double distance;
    private Point3D lightSource;

    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1; // 0001
    private static final int RIGHT = 2; // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP = 8; // 1000

    public Janim3D(int width, int height) {
        this.width = width;
        this.height = height;
        this.scale = 10; // Default scale
        this.distance = -50;

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clear();

        camPosition = new Point3D(20, 20, 20);
        camDirection = new Vector3D(camPosition, new Point3D(0, 0, 0)); // look at origin
        projection = Projection.PARALLEL; // Default projection
        lightSource = new Point3D(0, 0, 100);
        clearZBuffer();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScale() {
        return scale;
    }

    public Color getColor() {
        return color;
    }

    public BufferedImage getBuffer() {
        return buffer;
    }

    public Vector3D getCamDirection() {
        return camDirection;
    }

    public Point3D getCamPosition() {
        return camPosition;
    }

    public Projection getProjection() {
        return projection;
    }

    public double getDistance() {
        return distance;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setBuffer(BufferedImage buffer) {
        this.buffer = buffer;
    }

    public void setCamDirection(Vector3D direction) {
        camDirection = direction;
    }

    public void setCamPosition(Point3D position) {
        camPosition = position;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    private void drawPixel(int xScreen, int yScreen) {
        if (xScreen >= 0 && xScreen < width && yScreen >= 0 && yScreen < height) {
            buffer.setRGB(xScreen, yScreen, color.getRGB());
        }
    }

    public void clearZBuffer() {
        if (zBuffer == null || zBuffer.length != width || zBuffer[0].length != height) {
            zBuffer = new float[width][height];
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                zBuffer[x][y] = Float.POSITIVE_INFINITY;
            }
        }
    }

    public void clear() {
        int blue = new Color(0, 0, 100).getRGB();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer.setRGB(x, y, blue);
            }
        }
    }

    private void draw2DLine(int x0, int y0, int x1, int y1) {
        if (!cohenSutherlandClip(x0, y0, x1, y1)) {
            return; // Line is completely outside
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawPixel(x0, y0);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    // 3D line drawing with archive.projection and screen transform
    public void draw3DLine(Point3D p0, Point3D p1) {
        Point2D p0Proj, p1Proj;
        if (projection == Projection.PARALLEL) {
            p0Proj = ParallelProjection.project(p0, camDirection.opp(), camPosition);
            p1Proj = ParallelProjection.project(p1, camDirection.opp(), camPosition);
        } else if (projection == Projection.PERSPECTIVE) {
            p0Proj = PerspectiveProjection.project(p0, camDirection.opp(), camPosition, distance);
            p1Proj = PerspectiveProjection.project(p1, camDirection.opp(), camPosition, distance);
        } else {
            return;
        }

        // Transform universal Point2D to real Point2D
        Transform.toRealPoint2D(p0Proj, scale, width / 2, height / 2);
        Transform.toRealPoint2D(p1Proj, scale, width / 2, height / 2);

        draw2DLine((int) Math.round(p0Proj.x()), (int) Math.round(p0Proj.y()),
                (int) Math.round(p1Proj.x()), (int) Math.round(p1Proj.y()));
    }

    private void drawFilled3DQuad(Face3D face, Color pieceColor, boolean material) {
        Point2D[] screenPts = new Point2D[4];
        double[] depths = new double[4];
        double[] intensities = new double[4];
        double[] speculars = new double[4];

        Point3D[] vertices = face.getVertices();
        Vector3D normal = face.normal();
        normal.normalize();

        for (int i = 0; i < 4; ++i) {
            Point3D p = vertices[i];
            Point2D proj;

            if (projection == Projection.PARALLEL)
                proj = ParallelProjection.project(p, camDirection.opp(), camPosition);
            else
                proj = PerspectiveProjection.project(p, camDirection.opp(), camPosition, distance);

            Transform.toRealPoint2D(proj, scale, width / 2, height / 2);
            screenPts[i] = proj;
            depths[i] = p.z();

            Shading shading = computePhongIntensity(p, normal, lightSource, camPosition, material);
            intensities[i] = shading.intensity();
            speculars[i] = shading.specular();
        }

        rasterizeTriangle(screenPts[0], screenPts[1], screenPts[2],
                depths[0], depths[1], depths[2],
                intensities[0], intensities[1], intensities[2],
                speculars[0], speculars[1], speculars[2],
                pieceColor);

        rasterizeTriangle(screenPts[0], screenPts[2], screenPts[3],
                depths[0], depths[2], depths[3],
                intensities[0], intensities[2], intensities[3],
                speculars[0], speculars[2], speculars[3],
                pieceColor);
    }

    private Shading computePhongIntensity(Point3D point, Vector3D normal,
                                          Point3D lightPos, Point3D viewPos,
                                          boolean material) {
        Vector3D n = new Vector3D(normal); // Already normalized before call
        Vector3D l = new Vector3D(lightPos, point);
        l.normalize();

        Vector3D v = new Vector3D(viewPos, point);
        v.normalize();

        Vector3D r = new Vector3D(n);
        r.scale(2 * n.dot(l));
        r.minus(l);
        r.normalize();

        double ambient, specularStrength, shininess;

        if (material) {
            ambient = 0.3;
            specularStrength = 0.1;
            shininess = 16;
        } else {
            ambient = 0.3;
            specularStrength = 0.6;
            shininess = 64;
        }

        double diffuse = Math.max(0, n.dot(l.opp()));
        double specular = Math.pow(Math.max(0, r.dot(v)), shininess) * specularStrength;

        double intensity = ambient + diffuse;
        intensity = Math.min(1.0, Math.max(0.0, intensity));

        return new Shading(intensity, Math.min(1.0, specular));
    }

    private Color shadeColor(Color base, double intensity, double specular) {
        int r = (int)(base.getRed() * intensity + 255 * specular);
        int g = (int)(base.getGreen() * intensity + 255 * specular);
        int b = (int)(base.getBlue() * intensity + 255 * specular);
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    public void drawShape(Shape shape) {
        List<Line3D> lines = shape.getGridLines();
        for (Line3D line : lines) {
            draw3DLine(line.getP0(), line.getP1());
        }
    }

    private double min3(double a, double b, double c) {
        return Math.min(a, Math.min(b, c));
    }

    private double max3(double a, double b, double c) {
        return Math.max(a, Math.max(b, c));
    }

    private void rasterizeTriangle(
            Point2D p0, Point2D p1, Point2D p2,
            double z0, double z1, double z2,
            double i0, double i1, double i2,
            double s0, double s1, double s2,
            Color baseColor
    ) {
        int minX = (int) Math.max(0, Math.ceil(min3(p0.x(), p1.x(), p2.x())));
        int maxX = (int) Math.min(width - 1, Math.floor(max3(p0.x(), p1.x(), p2.x())));
        int minY = (int) Math.max(0, Math.ceil(min3(p0.y(), p1.y(), p2.y())));
        int maxY = (int) Math.min(height - 1, Math.floor(max3(p0.y(), p1.y(), p2.y())));

        double area = edgeFunction(p0, p1, p2);
        if (area == 0) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2D p = new Point2D(x, y);

                double w0 = edgeFunction(p1, p2, p);
                double w1 = edgeFunction(p2, p0, p);
                double w2 = edgeFunction(p0, p1, p);

                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                    w0 /= area;
                    w1 /= area;
                    w2 /= area;

                    double z = w0 * z0 + w1 * z1 + w2 * z2;
                    if (z < zBuffer[x][y]) {
                        zBuffer[x][y] = (float) z;

                        double intensity = w0 * i0 + w1 * i1 + w2 * i2;
                        double specular = w0 * s0 + w1 * s1 + w2 * s2;

                        this.color = shadeColor(baseColor, intensity, specular);
                        drawPixel(x, y);
                    }
                }
            }
        }
    }

    private double edgeFunction(Point2D a, Point2D b, Point2D c) {
        return (c.x() - a.x()) * (b.y() - a.y()) - (c.y() - a.y()) * (b.x() - a.x());
    }

    public void drawSolidShape(SolidShape shape) {
        List<Face3D> faces = shape.getFaces();
        for (Face3D face : faces) {
            // Background
            drawFilled3DQuad(face, new Color(10, 10, 10), false);

            // Sticker...;
            drawFilled3DQuad(
                    new Face3D(shrinkQuad(face.getVertices()), face.getColor()),
                    face.getColor(),
                    true
            );
        }
    }

    private Point3D[] shrinkQuad(Point3D[] quad) {
        double cx = 0, cy = 0, cz = 0;
        for (Point3D p : quad) {
            cx += p.x();
            cy += p.y();
            cz += p.z();
        }
        cx /= quad.length;
        cy /= quad.length;
        cz /= quad.length;

        Point3D center = new Point3D(cx, cy, cz);

        Point3D[] shrunken = new Point3D[quad.length];
        for (int i = 0; i < quad.length; i++) {
            Vector3D dir = new Vector3D(center, quad[i]);
            dir.scale(0.85);

            // Move slightly forward in the face's normal direction to avoid z-fighting
            Vector3D faceNormal = getQuadNormal(quad);
            faceNormal.normalize();
            shrunken[i] = new Point3D(
                    center.x() + dir.x() - faceNormal.x() * 0.01,
                    center.y() + dir.y() - faceNormal.y() * 0.01,
                    center.z() + dir.z() - faceNormal.z() * 0.01
            );
        }

        return shrunken;
    }

    private Vector3D getQuadNormal(Point3D[] quad) {
        // Assume convex quad, use first triangle for normal
        Vector3D u = new Vector3D(quad[0], quad[1]);
        Vector3D v = new Vector3D(quad[0], quad[3]);
        return u.cross(v);
    }

    // Cohen–Sutherland clipping
    private boolean cohenSutherlandClip(int x0, int y0, int x1, int y1) {
        int code0 = computeOutCode(x0, y0);
        int code1 = computeOutCode(x1, y1);

        while (true) {
            if ((code0 | code1) == 0) {
                return true; // Trivially accepted
            } else if ((code0 & code1) != 0) {
                return false; // Trivially rejected
            } else {
                int outCodeOut = (code0 != 0) ? code0 : code1;
                int x = 0, y = 0;

                if ((outCodeOut & TOP) != 0) {
                    x = x0 + (x1 - x0) * (-y0) / (y1 - y0);
                } else if ((outCodeOut & BOTTOM) != 0) {
                    x = x0 + (x1 - x0) * (height - 1 - y0) / (y1 - y0);
                    y = height - 1;
                } else if ((outCodeOut & RIGHT) != 0) {
                    y = y0 + (y1 - y0) * (width - 1 - x0) / (x1 - x0);
                    x = width - 1;
                } else if ((outCodeOut & LEFT) != 0) {
                    y = y0 + (y1 - y0) * (-x0) / (x1 - x0);
                }

                if (outCodeOut == code0) {
                    x0 = x;
                    y0 = y;
                    code0 = computeOutCode(x0, y0);
                } else {
                    x1 = x;
                    y1 = y;
                    code1 = computeOutCode(x1, y1);
                }
            }
        }
    }

    private int computeOutCode(int x, int y) {
        int code = INSIDE;
        if (x < 0) code |= LEFT;
        else if (x >= width) code |= RIGHT;
        if (y < 0) code |= TOP;
        else if (y >= height) code |= BOTTOM;
        return code;
    }

    public record Shading(double intensity, double specular) {}
}
