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
import polyrun.UnitNSphere;
import polyrun.constraints.ConstraintsSystem;

import java.util.Random;

/**
 * Ball walk sampler.
 */
public class BallWalk implements RandomWalk {

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
    public void next(double[][] A, int[][] indicesOfNonZeroElementsInA,
                     double[] b, double[] buffer,
                     double[] from, double[] to) {
        // Pick random direction by getting random point from hypersphere-sphere
        this.unitNSphere.fillVectorWithRandomPoint(buffer);

        // Pick length of a step
        final double step = getStepLength(this.radius, from.length);

        if (OutOfBoundsBehaviour.Stay.equals(this.outOfBoundsBehaviour)) {
            double[] nextPoint = new double[from.length];
            for (int i = 0; i < from.length; i++) {
                nextPoint[i] = from[i] + buffer[i] * step;
            }

            // Set newly generated point as a new if it is inside the polytope and stay otherwise
            if (ConstraintsSystem.isSatisfied(A, nextPoint, b)) {
                System.arraycopy(nextPoint, 0, to, 0, from.length);
            } else {
                System.arraycopy(from, 0, to, 0, from.length);
            }
        } else if (OutOfBoundsBehaviour.Crop.equals(this.outOfBoundsBehaviour)) {
            // Calculate distance from point 'from' to the boundary of the polytope defined by Ax <= b in direction stored in 'buffer'
            double distance = distanceToBoundary(A, b, buffer, from);

            if (Double.isNaN(distance)) {
                throw new RuntimeException("The region is unbounded or point 'from' is out of bounds.");
            }

            double[] nextPoint = new double[from.length];
            for (int i = 0; i < from.length; i++) {
                nextPoint[i] = from[i] + buffer[i] * Math.min(distance, step);
            }

            System.arraycopy(nextPoint, 0, to, 0, from.length);
        } else {
            throw new RuntimeException("Not supported outOfBoundsBehaviour.");
        }
    }

    /**
     * @param r radius
     * @param n number of dimensions
     * @return length of the step
     */
    protected double getStepLength(double r, int n) {
        return Math.pow(random.nextDouble(), 1.0 / (double) n) * r;
    }

    /**
     * Calculates distance from point x to the boundary of the polytope defined by Ax <= b
     * in the given direction.
     * <p>
     * The method calculates the minimum value of (b-Ax)_i/((Ad)_i) over all i such that 0 &lt; i &lt; m and (Ad)_i &gt; 0,
     * where m is the number of inequalities (rows in matrix A) and (v)_i is the i-th element of vector v.
     *
     * @param A matrix
     * @param b vector
     * @param d direction
     * @param x current point (vector)
     * @return distance to the boundary of the polytope from given point
     */
    private double distanceToBoundary(double[][] A, double[] b, double[] d, double[] x) {
        double result = Double.NaN;

        for (int j = 0; j < b.length; j++) {
            double ad = 0.0;
            double bax = b[j];

            for (int i = 0; i < A[0].length; i++) {
                ad += A[j][i] * d[i];
                bax -= A[j][i] * x[i];
            }

            if (ad > 0.0) {
                double nV = bax / ad;
                if (Double.isNaN(result) || result > nV) {
                    result = nV;
                }
            }
        }

        if (result < 0.0 && result > 1e-10) {
            // Take care about inaccuracy
            result = 0.0;
        }

        return result;
    }
}
