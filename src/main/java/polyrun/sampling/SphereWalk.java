package polyrun.sampling;


import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import java.util.Random;

/**
 * Sphere walk sampler.
 */
public class SphereWalk extends BallWalk {

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
        super(random, radius, outOfBoundsBehaviour);
    }

    @Override
    protected double getStepLength(double r, int n) {
        return r;
    }
}
