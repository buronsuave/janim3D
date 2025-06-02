package graphics;

import geometry.Line3D;
import geometry.Point2D;
import geometry.Point3D;
import geometry.Shape;
import geometry.Vector3D;
import math.Transform;
import projection.ParallelProjection;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class Janim3D {
    private int width;
    private int height;
    private int scale;
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
        this.scale = 10; // Default scale

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clear();

        camPosition = new Point3D(20,20,20);
        camDirection = new Vector3D(camPosition, new Point3D(0, 0, 0)); // look at origin
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getScale() { return scale; }
    public Color getColor() { return color; }
    public BufferedImage getBuffer() { return buffer; }
    public Vector3D getCamDirection() { return camDirection; }
    public Point3D getCamPosition() { return camPosition; }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setScale(int scale) { this.scale = scale; }
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
        /*Vector3D v0 = new Vector3D(camPosition, p0);
        Vector3D v1 = new Vector3D(camPosition, p1);

        if (v0.dot(camDirection) <= 0 && v1.dot(camDirection) <= 0) {
            return; // Both behind camera
        }*/

        Point2D p0Proj = ParallelProjection.project(p0, camDirection.opp(), camPosition);
        Point2D p1Proj = ParallelProjection.project(p1, camDirection.opp(), camPosition);

        // Transform universal Point2D to real Point2D
        Transform.toRealPoint2D(p0Proj, scale, width/2, height/2);
        Transform.toRealPoint2D(p1Proj, scale, width/2, height/2);

        draw2DLine ((int) Math.round(p0Proj.x()), (int) Math.round(p0Proj.y()),
                    (int) Math.round(p1Proj.x()), (int) Math.round(p1Proj.y()));
    }

    public void drawShape(Shape shape) {
        List<Line3D> lines = shape.getGridLines();
        for (Line3D line : lines) {
            draw3DLine(line.getP0(), line.getP1());
        }
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
