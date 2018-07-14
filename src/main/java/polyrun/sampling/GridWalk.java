package polyrun.sampling;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import polyrun.constraints.ConstraintsSystem;

import java.util.Random;

/**
 * Grid walk sampler.
 */
public class GridWalk implements RandomWalk {

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
    public void next(double[][] A, int[][] indicesOfNonZeroElementsInA,
                     double[] b, double[] buffer,
                     double[] from, double[] to) {
        int index = this.random.nextInt(2 * from.length);

        double[] nextPoint = new double[from.length];
        System.arraycopy(from, 0, nextPoint, 0, from.length);
        nextPoint[index / 2] = ((index % 2 == 0) ? 1.0 : -1.0) * this.gridSpacing;

        // Set newly generated point as a new if it is inside the polytope and stay otherwise
        if (ConstraintsSystem.isSatisfied(A, nextPoint, b)) {
            System.arraycopy(nextPoint, 0, to, 0, from.length);
        } else {
            System.arraycopy(from, 0, to, 0, from.length);
        }
    }
}
