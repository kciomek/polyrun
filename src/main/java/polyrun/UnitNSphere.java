package polyrun;

import java.util.Random;

/**
 * Represents n-sphere of unit radius centered at the origin.
 */
public class UnitNSphere {
    private final Random random;

    public UnitNSphere() {
        this.random = new Random();
    }

    public UnitNSphere(Random random) {
        this.random = random;
    }

    /**
     * Simple method of picking random point from unit n-sphere. It fills the vector (double[]) passed as first argument.
     * If the vector is provided in homogeneous coordinates, zero will be assigned to the last element, and
     * first elements will be filled with random point of unit n-sphere.
     * <p>
     * To get random point of unit n-sphere in Cartesian coordinates input vector has to be the length of n+1,
     * and in homogeneous coordinates coordinates vector has to be the length of n+2.
     *
     * @param vectorToFill vector to fill
     * @param homogeneous  whether provided vector is in homogeneous coordinates
     */
    public void fillVectorWithRandomPoint(double[] vectorToFill, boolean homogeneous) {
        double s = 0.0;
        int n;

        if (homogeneous) {
            n = vectorToFill.length - 1;
            vectorToFill[vectorToFill.length - 1] = 0.0;
        } else {
            n = vectorToFill.length;
        }

        for (int i = 0; i < n; i++) {
            vectorToFill[i] = random.nextGaussian();
            s += vectorToFill[i] * vectorToFill[i];
        }

        s = 1.0 / Math.sqrt(s);

        for (int i = 0; i < n; i++) {
            vectorToFill[i] *= s;
        }
    }
}
