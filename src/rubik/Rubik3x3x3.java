package rubik;

import geometry.Cube;
import geometry.SolidShape;
import graphics.Janim3D;
import math.Matrix;

import java.util.ArrayList;
import java.util.List;

public class Rubik3x3x3 {
    private final List<SolidShape> cubelets;
    private int[][] faces;

    public Rubik3x3x3() {
        cubelets = new ArrayList<>();
    }

    public Rubik3x3x3(double spacing, double cubeletSize) {
        cubelets = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    // Create cubelet
                    Cube cubelet = new Cube(cubeletSize);

                    // Position cubelet
                    cubelet.translate(x*spacing, y*spacing, z*spacing);
                    cubelet.setInitialTransform(cubelet.getTransformation());
                    cubelets.add(cubelet);
                }
            }
        }

        // Red, Orange, White, Yellow, Green, Blue
        faces = new int[][] {
                {18, 19, 20, 21, 22, 23, 24, 25, 26},
                {2, 1, 0, 5, 4, 3, 8, 7, 6},
                {6, 15, 24, 7, 16, 25, 8, 17, 26},
                {2, 11, 20, 1, 10, 19, 0, 9, 18},
                {20, 11, 2, 23, 14, 5, 26, 17, 8},
                {0, 9, 18, 3, 12, 21, 6, 15, 24}
        };
    }


    public void finishMovement(int faceIndex) {
        int[] oldIndexes = faces[faceIndex].clone();
        int[] newIndexes = new int[9];

        newIndexes[0] = oldIndexes[2];
        newIndexes[1] = oldIndexes[5];
        newIndexes[2] = oldIndexes[8];
        newIndexes[3] = oldIndexes[1];
        newIndexes[4] = oldIndexes[4]; // center doesn't move
        newIndexes[5] = oldIndexes[7];
        newIndexes[6] = oldIndexes[0];
        newIndexes[7] = oldIndexes[3];
        newIndexes[8] = oldIndexes[6];
        faces[faceIndex] = newIndexes;

        // Update the rest of the faces O(n**3)
        for (int i = 0; i < 6; ++i) {
            if (i == faceIndex) {
                continue;
            }

            int[] face = faces[i];
            for (int j = 0; j < 9; ++j) {
                for (int k = 0; k < 9; ++k) {
                    if (face[j] == oldIndexes[k]) {
                        face[j] = newIndexes[k];
                        break;
                    }
                }
            }
        }
    }

    public void moveFace(int faceIndex, double theta) {
        int[] faceIndexes = faces[faceIndex];

        for (int index : faceIndexes) {
            Cube cubelet = (Cube) cubelets.get(index);
            cubelet.resetTransformation();

            double[][] rotation = switch (faceIndex) {
                case 0, 1 -> Matrix.rotationXMatrix(theta);
                case 2, 3 -> Matrix.rotationYMatrix(theta);
                case 4, 5 -> Matrix.rotationZMatrix(theta);
                default -> throw new IllegalArgumentException("Invalid axis");
            };

            cubelet.setTransformation(Matrix.mul(
                    rotation,
                    cubelet.getTransformation()
            ));

            cubelet.setInitialTransform(cubelet.getTransformation());

            cubelet.setTransformation(Matrix.mul(
                    ((Cube) cubelets.get(13)).getTransformation(),
                    cubelet.getTransformation()
            ));
        }
    }

    // This works on every case without translation of the cube
    public void rotate(double t, char axis) {
        for (SolidShape solidShape : cubelets) {
            Cube cubelet = (Cube) solidShape;
            double[][] rotation = switch (axis) {
                case 'x' -> Matrix.rotationXMatrix(t);
                case 'y' -> Matrix.rotationYMatrix(t);
                case 'z' -> Matrix.rotationZMatrix(t);
                default -> throw new IllegalArgumentException("Invalid axis");
            };

            cubelet.setTransformation(Matrix.mul(
                    rotation,
                    cubelet.getTransformation()
            ));
        }
    }

    public void translate(double dx, double dy, double dz) {
        for (SolidShape solidShape : cubelets) {
            Cube cubelet = (Cube) solidShape;
            cubelet.setTransformation(
                    Matrix.mul(
                            Matrix.translationMatrix(dx, dy, dz),
                            cubelet.getTransformation()
                    )
            );
        }
    }

    public void scale(double sx, double sy, double sz) {
        for (SolidShape solidShape : cubelets) {
            Cube cubelet = (Cube) solidShape;
            cubelet.setTransformation(
                    Matrix.mul(
                            cubelet.getTransformation(),
                            Matrix.scaleMatrix(sx, sy, sz)
                    )
            );
        }
    }

    public void draw(Janim3D janim) {
        for (SolidShape cubelet : cubelets) {
            janim.drawSolidShape(cubelet, true);
        }
    }
}
