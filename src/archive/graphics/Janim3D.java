package archive.graphics;

import archive.geometry.Cube;
import archive.geometry.Point2D;
import archive.geometry.Point3D;
import archive.geometry.Vector3D;
import archive.projection.ParallelProjection;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Janim3D {
    private int width;
    private int height;
    private Color color = Color.WHITE; // Default color value
    private BufferedImage buffer;
    private Vector3D camDirection;
    private Point3D camPosition;

    private static final int INSIDE = 0; // 0000
    private static final int LEFT   = 1; // 0001
    private static final int RIGHT  = 2; // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP    = 8; // 1000

    public Janim3D(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clear();

        camPosition = new Point3D(2,2,2);
        camDirection = new Vector3D(camPosition, new Point3D(0, 0, 0)); // look at origin
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Color getColor() { return color; }
    public BufferedImage getBuffer() { return buffer; }
    public Vector3D getCamDirection() { return camDirection; }
    public Point3D getCamPosition() { return camPosition; }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setColor(Color color) { this.color = color; }
    public void setBuffer(BufferedImage buffer) { this.buffer = buffer; }
    public void setCamDirection(Vector3D direction) { camDirection = direction; }
    public void setCamPosition(Point3D position) { camPosition = position; }

    private void drawPixel(int xScreen, int yScreen) {
        if (xScreen >= 0 && xScreen < width && yScreen >= 0 && yScreen < height) {
            buffer.setRGB(xScreen, yScreen, color.getRGB());
        }
    }

    public void clear() {
        int black = new Color(0, 0, 0).getRGB();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer.setRGB(x, y, black);
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
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    // 3D line drawing with archive.projection and screen transform
    public void draw3DLine(Point3D p0, Point3D p1) {
        Vector3D v0 = new Vector3D(camPosition, p0);
        Vector3D v1 = new Vector3D(camPosition, p1);

        if (v0.dot(camDirection) <= 0 && v1.dot(camDirection) <= 0) {
            System.out.println(v0 + " and " + v1 + " behind camera");
            return; // Both behind camera
        }

        Point2D p0Proj = ParallelProjection.project(p0, camDirection);
        Point2D p1Proj = ParallelProjection.project(p1, camDirection);

        System.out.println("Projected points: " + p0Proj + "," + p1Proj);

        double scale = 75.0;
        double x0 = width / 2.0 + p0Proj.x() * scale;
        double y0 = height / 2.0 - p0Proj.y() * scale;
        double x1 = width / 2.0 + p1Proj.x() * scale;
        double y1 = height / 2.0 - p1Proj.y() * scale;

        System.out.println("Draw coordinates: (" + x0 + "," + y0 + ") to (" + x1 + "," + y1 + ")");

        draw2DLine((int) Math.round(x0), (int) Math.round(y0),
                (int) Math.round(x1), (int) Math.round(y1));
    }

    public void drawCube(Point3D center, int size) {
        Cube cube = new Cube(center, size);
        Point3D[] vertices = cube.getVertices();

        // Top
        draw3DLine(vertices[0], vertices[1]);
        System.out.println(vertices[0] + " to " + vertices[1]);

        //draw3DLine(vertices[1], vertices[2]);
        //draw3DLine(vertices[2], vertices[3]);
        //draw3DLine(vertices[3], vertices[0]);

        // Sides
        //draw3DLine(vertices[0], vertices[4]);
        //draw3DLine(vertices[1], vertices[5]);
        //draw3DLine(vertices[2], vertices[6]);
        //draw3DLine(vertices[3], vertices[7]);

        // Bottom
        //draw3DLine(vertices[4], vertices[5]);
        //draw3DLine(vertices[5], vertices[6]);
        //draw3DLine(vertices[6], vertices[7]);
        //draw3DLine(vertices[7], vertices[0]);
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
}
