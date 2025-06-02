package archive;

import archive.graphics.Janim3D;
import archive.geometry.Point3D;

import javax.swing.*;
import java.awt.*;

public class App extends JFrame {
    private final Janim3D janim;

    public App() {
        setTitle("Janim 3D");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        janim = new Janim3D(600, 600);
        drawAxes();

        // Draw some lines...
        janim.setColor(Color.WHITE);
        System.out.println("Drawing upper face of cube");
        janim.draw3DLine(new Point3D(1, 1, 1), new Point3D(-1, 1, 1));
        janim.draw3DLine(new Point3D(-1, 1, 1), new Point3D(-1, -1, 1));
        janim.draw3DLine(new Point3D(-1, -1, 1), new Point3D(1, -1, 1));
        janim.draw3DLine(new Point3D(1, -1, 1), new Point3D(1, 1, 1));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(janim.getBuffer(), 0, 0, null);
            }
        };
        panel.setOpaque(false);

        setContentPane(panel);
        setVisible(true);
    }

    private void drawAxes() {
        Point3D origin = new Point3D(0, 0, 0);

        // X axis (red)
        janim.setColor(Color.RED);
        janim.draw3DLine(origin, new Point3D(5, 0, 0));

        // Y axis (green)
        janim.setColor(Color.GREEN);
        janim.draw3DLine(origin, new Point3D(0, 5, 0));

        // Z axis (blue)
        janim.setColor(Color.BLUE);
        janim.draw3DLine(origin, new Point3D(0, 0, 5));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
