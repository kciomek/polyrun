package polyrun.sampling;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import java.util.Random;

/**
 * Grid walk sampler.
 */
public class GridWalk extends RandomWalk {

    private final Random random;
    private final double gridSpacing;

    /**
     * @param gridSpacing grid spacing
     */
    public GridWalk(double gridSpacing) {
        this(new RandomAdaptor(new MersenneTwister()), gridSpacing);
    }

    /**
     * @param random      random number generator
     * @param gridSpacing grid spacing
     */
    public GridWalk(Random random, double gridSpacing) {
        if (gridSpacing <= 0.0) {
            throw new IllegalArgumentException("Grid spacing have to be positive.");
        }

        this.random = random;
        this.gridSpacing = gridSpacing;
    }

    @Override
    protected void fillDirectionVector(double[] direction, boolean homogeneous) {
        for (int i = 0; i < direction.length; i++) {
            direction[i] = 0.0;
        }

        direction[this.random.nextInt() % (homogeneous ? direction.length : direction.length - 1)] = 1.0;
    }

    @Override
    protected double selectStepLength(int dim, double bg, double ed) {
        if (this.gridSpacing >= ed) {
            return 0.0;
        } else {
            return this.gridSpacing;
        }
    }
}
