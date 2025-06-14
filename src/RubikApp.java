import geometry.Point3D;
import rubik.Rubik3x3x3;
import geometry.Vector3D;
import graphics.Janim3D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RubikApp extends JFrame {

    // Camera configuration
    private static final int INITIAL_CAM_POSITION_X = 0;
    private static final int INITIAL_CAM_POSITION_Y = 0;
    private static final int INITIAL_CAM_POSITION_Z = 40;
    private final Point3D initialCamPosition = new Point3D(
            INITIAL_CAM_POSITION_X,
            INITIAL_CAM_POSITION_Y,
            INITIAL_CAM_POSITION_Z);
    private static final double DEFAULT_OBS_DISTANCE = -50;
    private static final Point3D ORIGIN = new Point3D(0, 0, 0);
    double radius;
    double angleX;
    double angleY;
    double angleZ;

    // Movement parameters
    private final double delta = Math.PI / 180; // ~0.5 degree
    private Timer autoRotateShapeTimer;
    private boolean autoRotateShape = false;
    private Timer moveFaceTimer;
    private boolean moveFace = false;
    double angleFace = 0;

    // Animation objects
    private final Janim3D janim;
    private final Rubik3x3x3 cube;
    private static final double CUBELET_SIZE = 10;
    private static final double CUBELET_SPACE = 10;

    // Frame parameters
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;

    public RubikApp() {
        setTitle("Janim 3D - Rubik");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        janim = new Janim3D(WIDTH, HEIGHT);
        janim.setCamPosition(initialCamPosition);
        janim.setCamDirection(new Vector3D(initialCamPosition, ORIGIN));
        janim.setDistance(DEFAULT_OBS_DISTANCE);
        initializeCameraAngles();

        // Draw cube
        cube = new Rubik3x3x3(CUBELET_SPACE, CUBELET_SIZE);
        cube.draw(janim);

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

        JPanel helpPanel = new JPanel();
        helpPanel.setOpaque(false);
        Font font = new Font("Code", Font.BOLD, 12);
        helpPanel.setLayout(new java.awt.GridLayout(0, 1));
        helpPanel.setBounds(WIDTH - 250, 10, 230, 300); // top-right position
        String[][] actions = {
                {"P", "Toggle Cube Auto-rotate"},
                {"O", "Move Orange CW"},
                {"R", "Move Red CW"},
                {"W", "Move White CW"},
                {"Y", "Move Yellow CW"},
                {"G", "Move Green CW"},
                {"B", "Move Blue CW"},
                {"Up / Down", "Rotate around X axis"},
                {"Left / Right", "Rotate around Y axis"},
                {"Z / X", "Rotate around Z axis"},
                {"S", "Scramble"}
        };

        for (String[] pair : actions) {
            JLabel newLabel = new JLabel(pair[0] + " - " + pair[1]);
            newLabel.setFont(font);
            newLabel.setForeground(Color.WHITE);
            helpPanel.add(newLabel);
        }

        panel.setLayout(null); // needed for absolute positioning
        panel.add(helpPanel);
        setVisible(true);

        bindKey(panel, "UP", () -> { cube.rotate(delta, 'x');  repaintScene(panel); });
        bindKey(panel, "DOWN", () -> { cube.rotate(-delta, 'x'); repaintScene(panel); });
        bindKey(panel, "LEFT", () -> { cube.rotate(delta, 'y');  repaintScene(panel); });
        bindKey(panel, "RIGHT", () -> { cube.rotate(-delta, 'y'); repaintScene(panel); });
        bindKey(panel, "Z", () -> { cube.rotate(delta, 'z');  repaintScene(panel); });
        bindKey(panel, "X", () -> { cube.rotate(-delta, 'z'); repaintScene(panel); });

        // Auto-rotation cube toggle
        bindKey(panel, "P", () -> {
            if (autoRotateShape) {
                autoRotateShapeTimer.stop();
            } else {
                AtomicInteger t = new AtomicInteger(0);
                autoRotateShapeTimer = new Timer(33, e -> {
                    t.incrementAndGet();
                    double time = t.get() / 60.0; // Slow and continuous
                    double angleX = delta * Math.sin(time / 3.0); // Slower variation
                    double angleY = delta * Math.sin(time / 4.0);
                    double angleZ = delta * Math.sin(time / 5.0);

                    cube.rotate(angleX, 'x');
                    cube.rotate(angleY, 'y');
                    cube.rotate(angleZ, 'z');
                    repaintScene(panel);
                });
                autoRotateShapeTimer.start();
            }
            autoRotateShape = !autoRotateShape;
            repaintScene(panel);
        });

        bindKey(panel, "R", () -> {
            moveFace(panel, 0, Math.PI/50);
        });

        bindKey(panel, "O", () -> {
            moveFace(panel, 1, -Math.PI/50);
        });

        bindKey(panel, "W", () -> {
            moveFace(panel, 2, Math.PI/50);
        });

        bindKey(panel, "Y", () -> {
            moveFace(panel, 3, -Math.PI/50);
        });

        bindKey(panel, "B", () -> {
            moveFace(panel, 4, Math.PI/50);
        });

        bindKey(panel, "G", () -> {
            moveFace(panel, 5, -Math.PI/50);
        });

        bindKey(panel, "S", () -> {
            scrambleCube(panel);
        });
    }

    private void scrambleCube(JPanel panel) {
        new Thread(() -> {
            Random random = new Random();
            int movesToDo = 20;

            for (int i = 0; i < movesToDo; i++) {
                try {
                    Thread.sleep(1000); // Wait 1 second between moves
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return; // Stop if interrupted
                }

                int face = random.nextInt(6);
                int dir = (face % 2 == 0) ? 1 : -1;
                double step = dir * (Math.PI / 50);

                // Wait until no movement is happening
                while (moveFace) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {}
                }

                // Schedule the move on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> moveFace(panel, face, step));
            }
        }).start();
    }

    private void moveFace(JPanel panel, int face, double step) {
        if (moveFace) return;
        else {
            moveFaceTimer = new Timer(33, e -> {
                angleFace += Math.PI/50;
                if (angleFace >= Math.PI/2) {
                    angleFace = 0; // Clamp to exact value
                    cube.finishMovement(face);
                    moveFaceTimer.stop();
                    moveFace = false;
                }

                cube.moveFace(face, step);
                repaintScene(panel);
            });

            moveFaceTimer.start();
        }

        moveFace = true;
        repaintScene(panel);
    }

    private void initializeCameraAngles() {
        double x = janim.getCamPosition().x();
        double y = janim.getCamPosition().y();
        double z = janim.getCamPosition().z();
        radius = Math.sqrt(x * x + y * y + z * z);

        angleX = Math.atan2(y, Math.sqrt(x * x + z * z));
        angleY = Math.atan2(x, z) - Math.PI/2;
        angleZ = Math.PI;
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
        cube.draw(janim);
    }

    private void repaintScene(JPanel panel) {
        janim.clear();
        janim.clearZBuffer();
        redrawScene();
        panel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RubikApp::new);
    }
}
