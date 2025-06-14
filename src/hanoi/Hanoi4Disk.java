package hanoi;

import geometry.SolidShape;
import geometry.Surface3D;
import graphics.Janim3D;

import java.awt.Color;

public class Hanoi4Disk {
    private final Surface3D[] disks;
    private final Peg[] pegs;

    public Hanoi4Disk() {
        disks = new Surface3D[] {
                new Surface3D(), new Surface3D(), new Surface3D(), new Surface3D()
        };

        pegs = new Peg[] {
                new Peg(), new Peg(), new Peg()
        };

        pegs[0].translate(50, -10, 0);
        pegs[1].translate(0, -10, 0);
        pegs[2].translate(-50, -10, 0);

        disks[3].setMainR(15);
        disks[3].setColor(new Color(9,147,209,255));
        disks[3].rotate(Math.PI/2, 'x');
        disks[3].translate(50, -7.5, 0);

        disks[2].setMainR(12);
        disks[2].setColor(new Color(182,36,36,255));
        disks[2].rotate(Math.PI/2, 'x');
        disks[2].translate(50, -2.5, 0);

        disks[1].setMainR(9);
        disks[1].setColor(new Color(29,146,68,255));
        disks[1].rotate(Math.PI/2, 'x');
        disks[1].translate(50, 2.5, 0);

        disks[0].setMainR(6);
        disks[0].setColor(new Color(192,160,25,255));
        disks[0].rotate(Math.PI/2, 'x');
        disks[0].translate(50, 7.5, 0);
    }

    public void moveDisk(int diskIndex, double dx, double dy, double dz) {
        disks[diskIndex].translate(dx, dy, dz);
    }

    public void draw(Janim3D janim) {
        for (SolidShape disk : disks) {
            janim.drawSolidShape(disk, false);
        }

        for (Peg peg : pegs) {
            janim.drawSolidShape(peg, false);
        }
    }
}
