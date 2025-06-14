import geometry.Point3D;
import hanoi.Hanoi4Disk;
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
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class HanoiApp extends JFrame {

    // Camera configuration
    private static final int INITIAL_CAM_POSITION_X = 0;
    private static final int INITIAL_CAM_POSITION_Y = 20;
    private static final int INITIAL_CAM_POSITION_Z = -40;
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
    private final double delta = 0.5;
    private Timer moveTimer;
    private boolean isMoving = false;
    private int pegSource = -1;
    private ArrayList<Integer>[] pegs = new ArrayList[3];
    private double auxX = 0;
    private double auxY = 0;

    // Animation objects
    private final Janim3D janim;
    private final Hanoi4Disk hanoi;

    // Frame parameters
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 1000;

    public HanoiApp() {
        setTitle("Janim 3D - Hanoi");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        janim = new Janim3D(WIDTH, HEIGHT);
        janim.setLightSource(new Point3D(0, 0, -30));
        janim.setScale(8);
        janim.setAmbientLight(0.5);

        janim.setCamPosition(initialCamPosition);
        janim.setCamDirection(new Vector3D(initialCamPosition, ORIGIN));
        janim.setDistance(DEFAULT_OBS_DISTANCE);
        initializeCameraAngles();

        hanoi = new Hanoi4Disk();
        hanoi.draw(janim);

        pegs[0] = new ArrayList<>();
        pegs[1] = new ArrayList<>();
        pegs[2] = new ArrayList<>();

        pegs[0].add(3);
        pegs[0].add(2);
        pegs[0].add(1);
        pegs[0].add(0);

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
        helpPanel.setBounds(WIDTH - 250, 10, 230, 100); // top-right position
        String[][] actions = {
                {"1/2/3", "First select source peg"},
                {"1/2/3", "Then select target peg"}
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

        bindKey(panel, "1", () -> {
            if (isMoving) return;
            if (pegSource == -1) {
                liftDisk(panel, 0);
            } else if (pegSource == 0) {
                releaseDisk(panel, 0);
            } else {
                moveSourceToPeg(panel, 0);
            }
        });

        bindKey(panel, "2", () -> {
            if (isMoving) return;
            if (pegSource == -1) {
                liftDisk(panel, 1);
            } else if (pegSource == 1) {
                releaseDisk(panel, 1);
            } else {
                moveSourceToPeg(panel, 1);
            }
        });

        bindKey(panel, "3", () -> {
            if (isMoving) return;
            if (pegSource == -1) {
                liftDisk(panel, 2);
            } else if (pegSource == 2) {
                releaseDisk(panel, 2);
            } else {
                moveSourceToPeg(panel, 2);
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
        hanoi.draw(janim);
    }

    private void repaintScene(JPanel panel) {
        janim.clear();
        janim.clearZBuffer();
        redrawScene();
        panel.repaint();
    }

    private void liftDisk(JPanel panel, int pegIndex) {
        if (isMoving) return;
        else {
            if (pegs[pegIndex].isEmpty()) return;
            auxY = -7.5 + 5*pegs[pegIndex].size();

            moveTimer = new Timer(33, e -> {
                if (auxY >= 32.5) {
                    moveTimer.stop();
                    isMoving = false;
                    pegSource = pegIndex;
                }

                auxY += delta;
                hanoi.moveDisk(pegs[pegIndex].get(pegs[pegIndex].size()-1), 0, delta, 0);
                repaintScene(panel);
            });

            moveTimer.start();
        }

        isMoving = true;
        repaintScene(panel);
    }

    private void releaseDisk(JPanel panel, int pegIndex) {
        if (isMoving) return;
        else {

            if (pegs[pegIndex].isEmpty()) return;
            auxY = 32.5;
            double targetY = -7.5 + 5*(pegs[pegIndex].size());

            moveTimer = new Timer(33, e -> {
                if (auxY <= targetY) {
                    moveTimer.stop();
                    isMoving = false;
                    pegSource = -1;
                }

                auxY -= delta;
                hanoi.moveDisk(pegs[pegIndex].get(pegs[pegIndex].size()-1), 0, -delta, 0);
                repaintScene(panel);
            });

            moveTimer.start();
        }

        isMoving = true;
        repaintScene(panel);
    }

    private void moveSourceToPeg(JPanel panel, int pegIndex) {
        if (isMoving) return;
        else {
            if (!pegs[pegIndex].isEmpty() &&
                    pegs[pegSource].get(pegs[pegSource].size()-1) > pegs[pegIndex].get(pegs[pegIndex].size()-1))
                return;

            int sign = (pegSource > pegIndex) ? 1 : -1;
            auxX = 50 - 50*pegSource;
            double targetX = 50 - 50*pegIndex;

            moveTimer = new Timer(33, e -> {
                if ((sign > 0 && auxX >= targetX) || (sign < 0 && auxX <= targetX)) {
                    moveTimer.stop();
                    // Testing...
                    hanoi.moveDisk(pegs[pegSource].get(pegs[pegSource].size()-1), -(sign*delta), 0, 0);

                    double targetY = -7.5 + 5*(pegs[pegIndex].size()+1);
                    auxY = 32.5;

                    moveTimer = new Timer(33, e2 -> {
                        if (auxY <= targetY) {
                            moveTimer.stop();
                            // Testing
                            hanoi.moveDisk(pegs[pegSource].get(pegs[pegSource].size()-1), 0, -delta, 0);
                            repaintScene(panel);

                            int disk = pegs[pegSource].get(pegs[pegSource].size()-1);
                            pegs[pegIndex].add(disk);
                            pegs[pegSource].remove(Integer.valueOf(disk));

                            isMoving = false;
                            pegSource = -1;

                            return;
                        }

                        auxY -= delta;
                        hanoi.moveDisk(pegs[pegSource].get(pegs[pegSource].size()-1), 0, -delta, 0);
                        repaintScene(panel);
                    });

                    moveTimer.start();
                }

                auxX += (sign*delta);
                hanoi.moveDisk(pegs[pegSource].get(pegs[pegSource].size()-1), (sign*delta), 0, 0);
                repaintScene(panel);
            });

            moveTimer.start();
        }

        isMoving = true;
        repaintScene(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HanoiApp::new);
    }
}