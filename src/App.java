import geometry.Cube;
import geometry.Point3D;
import geometry.Vector3D;
import graphics.Janim3D;
import math.Transform;

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
    private double angleX;
    private double angleY;
    private double angleZ;
    private double radius;
    private static final Point3D ORIGIN = new Point3D(0, 0, 0);

    // Movement parameters
    private final double delta = Math.PI / 90; // 2 degrees per key-press
    private Timer autoRotateTimer;
    private Timer autoRotateCubeTimer;
    private boolean autoRotate = false;
    private boolean autoRotateCube = false;

    // Animation objects
    private final Janim3D janim;
    private final Cube cube;
    private static final int CUBE_SIZE = 10;

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
        initializeCameraAngles();

        drawAxes();

        // Draw cube
        cube = new Cube(CUBE_SIZE);
        janim.setColor(Color.WHITE);
        janim.drawShape(cube);

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
                {"T/G", "Rotate Cube X"},
                {"Y/H", "Rotate Cube Y"},
                {"U/J", "Rotate Cube Z"},
                {"Arrows", "Translate XY"},
                {"Z/X", "Translate Z"},
                {"C",   "Reset Cube"},
                {"F",   "Toggle Cube Auto-rotate"},
                {"N/M", "Scale Cube"},
                {"L",   "Print Cube on Serial"}
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
            initializeCameraAngles();  // recalculate angles from original
            updateCameraPosition();
            repaintScene(panel);
        });
        // Auto-rotation toggle
        bindKey(panel, "P", () -> {
            if (autoRotate) {
                autoRotateTimer.stop();
            } else {
                radius = janim.getCamDirection().length(); // ensure same radius
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
        bindKey(panel, "T", () -> { cube.rotate(delta, 'x');  repaintScene(panel); });
        bindKey(panel, "G", () -> { cube.rotate(-delta, 'x'); repaintScene(panel); });
        bindKey(panel, "Y", () -> { cube.rotate(delta, 'y');  repaintScene(panel); });
        bindKey(panel, "H", () -> { cube.rotate(-delta, 'y'); repaintScene(panel); });
        bindKey(panel, "U", () -> { cube.rotate(delta, 'z');  repaintScene(panel); });
        bindKey(panel, "J", () -> { cube.rotate(-delta, 'z'); repaintScene(panel); });
        bindKey(panel, "UP", () -> { cube.translate(0,0.5,0);  repaintScene(panel); });
        bindKey(panel, "DOWN", () -> { cube.translate(0,-0.5,0); repaintScene(panel); });
        bindKey(panel, "LEFT", () -> { cube.translate(-0.5,0,0);  repaintScene(panel); });
        bindKey(panel, "RIGHT", () -> { cube.translate(0.5,0,0); repaintScene(panel); });
        bindKey(panel, "Z", () -> { cube.translate(0,0,0.5);  repaintScene(panel); });
        bindKey(panel, "X", () -> { cube.translate(0,0,-0.5); repaintScene(panel); });
        bindKey(panel, "C", () -> { cube.resetTransformation(); repaintScene(panel); });
        // Auto-rotation cube toggle
        bindKey(panel, "F", () -> {
            if (autoRotateCube) {
                autoRotateCubeTimer.stop();
            } else {
                AtomicInteger t = new AtomicInteger(0);
                autoRotateCubeTimer = new Timer(30, e -> {
                    t.incrementAndGet();
                    cube.rotate(delta * Math.abs(Math.sin(t.get() / (40*Math.PI))), 'x');
                    cube.rotate(delta * Math.abs(Math.sin(t.get() / (30*Math.PI))), 'y');
                    cube.rotate(delta * Math.abs(Math.sin(t.get() / (50*Math.PI))), 'z');
                    repaintScene(panel);
                });
                autoRotateCubeTimer.start();
            }
            autoRotateCube = !autoRotateCube;
            repaintScene(panel);
        });

        bindKey(panel, "N", () -> { cube.scale(0.95, 0.95, 0.95);  repaintScene(panel); });
        bindKey(panel, "M", () -> { cube.scale(1.05,1.05,1.05); repaintScene(panel); });

        // Debug
        bindKey(panel, "L", () -> System.out.println(cube));
    }

    private void initializeCameraAngles() {
        double x = initialCamPosition.x();
        double y = initialCamPosition.y();
        double z = initialCamPosition.z();
        radius = Math.sqrt(x * x + y * y + z * z);

        angleX = Math.atan2(y, Math.sqrt(x * x + z * z));
        angleY = Math.atan2(x, z) - Math.PI/2;
        angleZ = Math.PI;
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
        janim.drawShape(cube);
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
