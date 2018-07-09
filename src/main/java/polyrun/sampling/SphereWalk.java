package polyrun.sampling;


import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import polyrun.UnitNSphere;

import java.util.Random;

/**
 * Sphere walk sampler.
 */
public class SphereWalk extends RandomWalk {

    private final UnitNSphere unitNSphere;
    private final double radius;
    private final OutOfBoundsBehaviour outOfBoundsBehaviour;

    /**
     * @param radius               radius of a sphere
     * @param outOfBoundsBehaviour the behaviour of random walk when it tries to make a step that exceeds bounds of the polytope
     */
    public SphereWalk(double radius, OutOfBoundsBehaviour outOfBoundsBehaviour) {
        this(new RandomAdaptor(new MersenneTwister()), radius, outOfBoundsBehaviour);
    }

    /**
     * @param random               random number generator
     * @param radius               radius of a sphere
     * @param outOfBoundsBehaviour the behaviour of random walk when it tries to make a step that exceeds bounds of the polytope
     */
    public SphereWalk(Random random, double radius, OutOfBoundsBehaviour outOfBoundsBehaviour) {
        if (radius <= 0.0) {
            throw new IllegalArgumentException("Radius have to be positive.");
        }

        this.unitNSphere = new UnitNSphere(random);
        this.radius = radius;
        this.outOfBoundsBehaviour = outOfBoundsBehaviour;
    }

    @Override
    protected void fillDirectionVector(double[] direction, boolean homogeneous) {
        this.unitNSphere.fillVectorWithRandomPoint(direction, homogeneous);
    }

    @Override
    protected double selectStepLength(int dim, double bg, double ed) {
        if (OutOfBoundsBehaviour.Stay.equals(this.outOfBoundsBehaviour)) {
            return this.radius > ed ? 0.0 : this.radius;
        } else if (OutOfBoundsBehaviour.Crop.equals(this.outOfBoundsBehaviour)) {
            return Math.min(ed, this.radius);
        } else {
            throw new RuntimeException("Not supported outOfBoundsBehaviour.");
        }
    }
}
