import geometry.Cube;
import geometry.Curve3D;
import geometry.GaussSurface3D;
import geometry.Point3D;
import geometry.Shape;
import geometry.SqueezedCylinder;
import geometry.Surface3D;
import geometry.Vector3D;
import graphics.Janim3D;
import math.Transform;
import projection.Projection;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class App extends JFrame {

    // Camera configuration
    private static final int INITIAL_CAM_POSITION_X = 20;
    private static final int INITIAL_CAM_POSITION_Y = 20;
    private static final int INITIAL_CAM_POSITION_Z = 20;
    private final Point3D initialCamPosition = new Point3D(
            INITIAL_CAM_POSITION_X,
            INITIAL_CAM_POSITION_Y,
            INITIAL_CAM_POSITION_Z);
    private static final double DEFAULT_OBS_DISTANCE = -50;
    private double angleX;
    private double angleY;
    private double angleZ;
    private double radius;
    private static final Point3D ORIGIN = new Point3D(0, 0, 0);

    // Movement parameters
    private final double delta = Math.PI / 90; // 2 degrees per key-press
    private Timer autoRotateTimer;
    private Timer autoRotateShapeTimer;
    private boolean autoRotate = false;
    private boolean autoRotateShape = false;

    // Animation objects
    private final Janim3D janim;
    private final Cube cube;
    private static final int CUBE_SIZE = 10;
    private final Curve3D curve;
    private final Surface3D surface;
    private final SqueezedCylinder cylinder;
    private final GaussSurface3D gauss;
    private Shape currentShape;

    // Frame parameters
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;

    // Axes parameters
    private static final int AXIS_LENGTH = 50;

    public App() {
        setTitle("Janim 3D");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        janim = new Janim3D(WIDTH, HEIGHT);
        janim.setCamPosition(initialCamPosition);
        janim.setCamDirection(new Vector3D(initialCamPosition, ORIGIN));
        janim.setDistance(DEFAULT_OBS_DISTANCE);
        initializeCameraAngles();

        drawAxes();

        // Draw cube
        cube = new Cube(CUBE_SIZE);
        curve = new Curve3D();
        surface = new Surface3D();
        cylinder = new SqueezedCylinder();
        gauss = new GaussSurface3D();
        currentShape = cube;

        janim.setColor(Color.WHITE);
        janim.drawShape(currentShape);

        // Panel for drawing
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(janim.getBuffer(), 0, 0, null);
            }
        };
        panel.setOpaque(false);
        setContentPane(panel);
        // Add help panel with buttons in top-right corner
        JPanel helpPanel = new JPanel();
        helpPanel.setOpaque(false);
        helpPanel.setBackground(new Color(0, 0, 0, 180));
        helpPanel.setForeground(Color.WHITE);
        helpPanel.setLayout(new java.awt.GridLayout(0, 1));
        helpPanel.setBounds(WIDTH - 250, 10, 230, 300); // top-right position

        String[][] actions = {
                {"Q/A", "Rotate Cam X"},
                {"W/S", "Rotate Cam Y"},
                {"E/D", "Rotate Cam Z"},
                {"R",   "Reset Cam"},
                {"P",   "Toggle Cam Auto-rotate"},
                {"T/G", "Rotate Shape X"},
                {"Y/H", "Rotate Shape Y"},
                {"U/J", "Rotate Shape Z"},
                {"Arrows", "Translate Shape XY"},
                {"Z/X", "Translate Shape Z"},
                {"C",   "Reset Shape"},
                {"F",   "Toggle Shape Auto-rotate"},
                {"N/M", "Scale Shape"},
                {"L",   "Print Shape on Serial"},
                {"V",   "Toggle Projection"},
                {"1/2", "Move observer's distance"},
                {"3", "Select cube"},
                {"4", "Select curve"},
                {"5", "Select surface"},
                {"6", "Select squeezed cylinder"},
                {"7", "Select Gaussian surface"}
        };

        for (String[] pair : actions) {
            helpPanel.add(new javax.swing.JLabel(pair[0] + " - " + pair[1]));
        }

        panel.setLayout(null); // needed for absolute positioning
        panel.add(helpPanel);

        setVisible(true);

        // Camera control
        bindKey(panel, "Q", () -> { angleX += delta; updateCameraPosition(); repaintScene(panel); });
        bindKey(panel, "A", () -> { angleX -= delta; updateCameraPosition(); repaintScene(panel); });
        bindKey(panel, "W", () -> { angleY += delta; updateCameraPosition(); repaintScene(panel); });
        bindKey(panel, "S", () -> { angleY -= delta; updateCameraPosition(); repaintScene(panel); });
        bindKey(panel, "E", () -> { angleZ += delta; updateCameraPosition(); repaintScene(panel); });
        bindKey(panel, "D", () -> { angleZ -= delta; updateCameraPosition(); repaintScene(panel); });
        bindKey(panel, "R", () -> {
            angleX = 0;
            angleY = 0;
            angleZ = 0;
            janim.setCamPosition(initialCamPosition);
            janim.setCamDirection(new Vector3D(initialCamPosition, ORIGIN));
            radius = janim.getCamDirection().length();
            janim.setDistance(DEFAULT_OBS_DISTANCE);
            initializeCameraAngles();  // recalculate angles from original
            updateCameraPosition();
            repaintScene(panel);
        });
        // Auto-rotation toggle
        bindKey(panel, "P", () -> {
            if (autoRotate) {
                autoRotateTimer.stop();
            } else {
                autoRotateTimer = new Timer(30, e -> {
                    angleY += delta / 5; // smooth slow orbit
                    updateCameraPosition();
                    repaintScene(panel);
                });
                autoRotateTimer.start();
            }
            autoRotate = !autoRotate;
            repaintScene(panel);
        });

        // Cube control
        bindKey(panel, "T", () -> { currentShape.rotate(delta, 'x');  repaintScene(panel); });
        bindKey(panel, "G", () -> { currentShape.rotate(-delta, 'x'); repaintScene(panel); });
        bindKey(panel, "Y", () -> { currentShape.rotate(delta, 'y');  repaintScene(panel); });
        bindKey(panel, "H", () -> { currentShape.rotate(-delta, 'y'); repaintScene(panel); });
        bindKey(panel, "U", () -> { currentShape.rotate(delta, 'z');  repaintScene(panel); });
        bindKey(panel, "J", () -> { currentShape.rotate(-delta, 'z'); repaintScene(panel); });
        bindKey(panel, "UP", () -> { currentShape.translate(0,0.5,0);  repaintScene(panel); });
        bindKey(panel, "DOWN", () -> { currentShape.translate(0,-0.5,0); repaintScene(panel); });
        bindKey(panel, "LEFT", () -> { currentShape.translate(-0.5,0,0);  repaintScene(panel); });
        bindKey(panel, "RIGHT", () -> { currentShape.translate(0.5,0,0); repaintScene(panel); });
        bindKey(panel, "Z", () -> { currentShape.translate(0,0,0.5);  repaintScene(panel); });
        bindKey(panel, "X", () -> { currentShape.translate(0,0,-0.5); repaintScene(panel); });
        bindKey(panel, "C", () -> { currentShape.resetTransformation(); repaintScene(panel); });
        // Auto-rotation cube toggle
        bindKey(panel, "F", () -> {
            if (autoRotateShape) {
                autoRotateShapeTimer.stop();
            } else {
                AtomicInteger t = new AtomicInteger(0);
                autoRotateShapeTimer = new Timer(30, e -> {
                    t.incrementAndGet();
                    currentShape.rotate(delta * Math.abs(Math.sin(t.get() / (40*Math.PI))), 'x');
                    currentShape.rotate(delta * Math.abs(Math.sin(t.get() / (30*Math.PI))), 'y');
                    currentShape.rotate(delta * Math.abs(Math.sin(t.get() / (50*Math.PI))), 'z');
                    repaintScene(panel);
                });
                autoRotateShapeTimer.start();
            }
            autoRotateShape = !autoRotateShape;
            repaintScene(panel);
        });

        bindKey(panel, "N", () -> { currentShape.scale(0.95, 0.95, 0.95);  repaintScene(panel); });
        bindKey(panel, "M", () -> { currentShape.scale(1.05,1.05,1.05); repaintScene(panel); });

        // Debug
        bindKey(panel, "L", () -> System.out.println(currentShape + ", d=" + janim.getDistance()));

        bindKey(panel, "1", () -> {
            janim.setDistance(janim.getDistance() + 0.5);
            repaintScene(panel);
        });

        bindKey(panel, "2", () -> {
            janim.setDistance(janim.getDistance() - 0.5);
            repaintScene(panel);
        });

        // Set cube as current shape
        bindKey(panel, "3", () -> {
            currentShape = cube;
            repaintScene(panel);
        });

        // Set curve as current shape
        bindKey(panel, "4", () -> {
            currentShape = curve;
            repaintScene(panel);
        });

        // Set surface as current shape
        bindKey(panel, "5", () -> {
            currentShape = surface;
            repaintScene(panel);
        });

        // Set squeezed cylinder as current shape
        bindKey(panel, "6", () -> {
            currentShape = cylinder;
            repaintScene(panel);
        });

        // Set gauss surface as current shape
        bindKey(panel, "7", () -> {
            currentShape = gauss;
            repaintScene(panel);
        });

        // Toggle projection
        bindKey(panel, "V", () -> {
            if (janim.getProjection() == Projection.PARALLEL) {
                janim.setProjection(Projection.PERSPECTIVE);
                updateCameraPosition();
                repaintScene(panel);
            }
            else if (janim.getProjection() == Projection.PERSPECTIVE) {
                janim.setProjection(Projection.PARALLEL);
                janim.setCamPosition(initialCamPosition);
                updateCameraPosition();
                repaintScene(panel);
            }
        });
    }

    private void initializeCameraAngles() {
        double x = janim.getCamPosition().x();
        double y = janim.getCamPosition().y();
        double z = janim.getCamPosition().z();
        radius = Math.sqrt(x * x + y * y + z * z);

        angleX = Math.atan2(y, Math.sqrt(x * x + z * z));
        angleY = Math.atan2(x, z) - Math.PI/2;
        angleZ = Math.PI;
        //angleZ = 0;
    }

    private void updateCameraPosition() {
        // Start from a point on +Z axis at distance `radius`
        Point3D pos = new Point3D(0, 0, radius);

        // Apply ZYX rotation order
        Transform.rotatePoint3DAroundX(pos, angleX);
        Transform.rotatePoint3DAroundY(pos, angleY);
        Transform.rotatePoint3DAroundZ(pos, angleZ);

        janim.setCamPosition(pos);
        janim.setCamDirection(new Vector3D(pos, ORIGIN));
    }

    private void bindKey(JComponent comp, String key, Runnable action) {
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        comp.getActionMap().put(key, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void redrawScene() {
        // Axes
        drawAxes();

        // Cube
        janim.setColor(Color.WHITE);
        janim.drawShape(currentShape);
    }

    private void repaintScene(JPanel panel) {
        janim.clear();
        redrawScene();
        panel.repaint();
    }

    private void drawAxes() {
        // Draw axes
        janim.setColor(Color.RED);
        janim.draw3DLine(ORIGIN, new Point3D(AXIS_LENGTH, 0, 0));
        janim.setColor(Color.GREEN);
        janim.draw3DLine(ORIGIN, new Point3D(0, AXIS_LENGTH, 0));
        janim.setColor(Color.BLUE);
        janim.draw3DLine(ORIGIN, new Point3D(0, 0, AXIS_LENGTH));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
