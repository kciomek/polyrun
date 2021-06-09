// Copyright (c) 2015-2018 Krzysztof Ciomek
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

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
        // Pick random direction parallel to one of the axes:
        // index / 2 - the index of axis
        // index % 2 - the direction (0 - up, 1 - down)
        int index = this.random.nextInt(2 * from.length);

        double[] nextPoint = new double[from.length];
        System.arraycopy(from, 0, nextPoint, 0, from.length);
        nextPoint[index / 2] = ((index % 2 == 0) ? 1.0 : -1.0) * this.gridSpacing;

        // Set newly generated point as a new if it is inside the polytope and stay otherwise
        if (ConstraintsSystem.isSatisfied(A, nextPoint, b, 1e-10)) {
            System.arraycopy(nextPoint, 0, to, 0, from.length);
        } else {
            System.arraycopy(from, 0, to, 0, from.length);
        }
    }
}
