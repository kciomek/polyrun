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
import polyrun.Boundary;
import polyrun.UnitNSphere;

import java.util.Random;

/**
 * HitAndRun sampler.
 */
public class HitAndRun implements RandomWalk {

    private final Random random;
    private final UnitNSphere unitNSphere;
    private final Boundary boundary;

    public HitAndRun() {
        this(new RandomAdaptor(new MersenneTwister()));
    }

    public HitAndRun(Random random) {
        this.random = random;
        this.unitNSphere = new UnitNSphere(random);
        this.boundary = new Boundary();
    }

    public void next(double[][] A, int[][] indicesOfNonZeroElementsInA,
                     double[] b, double[] buffer,
                     double[] from, double[] to) {

        // Generate random direction (pick a random point from unit hypersphere)
        this.unitNSphere.fillVectorWithRandomPoint(buffer);

        // Calculate begin and end of the segment along the generated direction (distance to boundary in both directions)
        double[] dist = boundary.distance(A, b, buffer, from, 1e-10, indicesOfNonZeroElementsInA);

        if (dist[0] == Double.POSITIVE_INFINITY || dist[1] == Double.NEGATIVE_INFINITY) {
            throw new RuntimeException("Cannot find begin or end of a segment for given direction. The sampling region is unbounded.");
        }

        if (dist[1] >= dist[0]) {
            // dist[0] == dist[1] == 0.0 => polytope defined by provided set of inequalities Ax <= b is not full-dimensional
            // dist[1] > dist[0] => should not happen (see class Boundary)
            // above cases are considered together due to computer inaccuracy; possible split into two conditions:
            throw new RuntimeException("Polytope is not full-dimensional.");

            // it also fails if sampler will be right in one of the vertices and direction 'd' will not allow to move anywhere; the probability of such situation goes to zero
        }

        // Select a step size
        double stepLength = (dist[1] + (dist[0] - dist[1]) * this.random.nextDouble());

        // Set destination
        for (int i = 0; i < A[0].length; i++) {
            to[i] = buffer[i] * stepLength + from[i];
        }
    }
}
