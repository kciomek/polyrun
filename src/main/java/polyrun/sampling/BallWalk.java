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
    private final boolean reflection;

    /**
     * @param radius     radius of a ball
     * @param reflection whether to reflect a step ({@code true}) or crop to edge ({@code false}) when step exceeds polytope
     */
    public BallWalk(double radius, boolean reflection) {
        this(new RandomAdaptor(new MersenneTwister()), radius, reflection);
    }

    /**
     * @param random     random number generator
     * @param radius     radius of a ball
     * @param reflection whether to reflect a step ({@code true}) or crop to edge ({@code false}) when step exceeds polytope
     */
    public BallWalk(Random random, double radius, boolean reflection) {
        if (radius <= 0.0) {
            throw new IllegalArgumentException("Radius have to be positive.");
        }

        this.unitNSphere = new UnitNSphere(random);
        this.radius = radius;
        this.reflection = reflection;
    }

    @Override
    protected void fillDirectionVector(double[] direction, boolean homogeneous) {
        this.unitNSphere.fillVectorWithRandomPoint(direction, homogeneous);
    }

    @Override
    protected double selectStepLength(double[] direction, double bg, double ed) {
        if (this.reflection) {
            if (this.radius > ed) {
                double reflected = ed - (this.radius - ed);

                return reflected < bg ? 0.0 : reflected;
            } else {
                return this.radius;
            }
        } else {
            return Math.min(ed, this.radius);
        }
    }
}
