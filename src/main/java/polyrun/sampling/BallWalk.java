package polyrun.sampling;


import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import polyrun.UnitNSphere;

import java.util.Random;

/**
 * Ball walk sampler.
 */
public class BallWalk extends RandomWalk {

    private final UnitNSphere unitNSphere;
    private final double radius;
    private final OutOfBoundsBehaviour outOfBoundsBehaviour;
    private final Random random;

    /**
     * @param radius radius of a ball
     */
    public BallWalk(double radius) {
        this(new RandomAdaptor(new MersenneTwister()), radius, OutOfBoundsBehaviour.Stay);
    }

    /**
     * @param radius               radius of a ball
     * @param outOfBoundsBehaviour the behaviour of random walk when it tries to make a step that exceeds bounds of the polytope
     */
    public BallWalk(double radius, OutOfBoundsBehaviour outOfBoundsBehaviour) {
        this(new RandomAdaptor(new MersenneTwister()), radius, outOfBoundsBehaviour);
    }

    /**
     * @param random               random number generator
     * @param radius               radius of a ball
     * @param outOfBoundsBehaviour the behaviour of random walk when it tries to make a step that exceeds bounds of the polytope
     */
    public BallWalk(Random random, double radius, OutOfBoundsBehaviour outOfBoundsBehaviour) {
        if (radius <= 0.0) {
            throw new IllegalArgumentException("Radius have to be positive.");
        }

        this.unitNSphere = new UnitNSphere(random);
        this.radius = radius;
        this.outOfBoundsBehaviour = outOfBoundsBehaviour;
        this.random = random;
    }

    @Override
    protected void fillDirectionVector(double[] direction, boolean homogeneous) {
        this.unitNSphere.fillVectorWithRandomPoint(direction, homogeneous);
    }

    @Override
    protected double selectStepLength(int dim, double bg, double ed) {
        double step = Math.pow(random.nextDouble(), 1.0 / (double) dim) * this.radius;

        if (OutOfBoundsBehaviour.Stay.equals(this.outOfBoundsBehaviour)) {
            return step > ed ? 0.0 : step;
        } else if (OutOfBoundsBehaviour.Crop.equals(this.outOfBoundsBehaviour)) {
            return Math.min(ed, step);
        } else {
            throw new RuntimeException("Not supported outOfBoundsBehaviour.");
        }
    }
}
