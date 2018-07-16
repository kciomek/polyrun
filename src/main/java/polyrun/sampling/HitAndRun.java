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

import java.util.Random;

/**
 * HitAndRun sampler.
 */
public class HitAndRun implements RandomWalk {

    private final Random random;
    private final UnitNSphere unitNSphere;

    public HitAndRun() {
        this(new RandomAdaptor(new MersenneTwister()));
    }

    public HitAndRun(Random random) {
        this.random = random;
        this.unitNSphere = new UnitNSphere(random);
    }

    public void next(double[][] A, int[][] indicesOfNonZeroElementsInA,
                     double[] b, double[] buffer,
                     double[] from, double[] to) {

        // Generate direction
        this.unitNSphere.fillVectorWithRandomPoint(buffer);

        // Calculate begin and end of the segment along the generated direction
        double bg = Double.NaN;
        double ed = Double.NaN;

        if (indicesOfNonZeroElementsInA != null) {
            for (int j = 0; j < b.length; j++) {
                double ad = 0.0;
                double bax = b[j];

                for (int i : indicesOfNonZeroElementsInA[j]) {
                    ad += A[j][i] * buffer[i];
                    bax -= A[j][i] * from[i];
                }

                if (ad < 0.0) {
                    double nV = bax / ad;
                    if (Double.isNaN(bg) || bg < nV) {
                        bg = nV;
                    }
                } else if (ad > 0.0) {
                    double nV = bax / ad;
                    if (Double.isNaN(ed) || ed > nV) {
                        ed = nV;
                    }
                }
            }
        } else {
            for (int j = 0; j < b.length; j++) {
                double ad = 0.0;
                double bax = b[j];

                for (int i = 0; i < A[0].length; i++) {
                    ad += A[j][i] * buffer[i];
                    bax -= A[j][i] * from[i];
                }

                if (ad < 0.0) {
                    double nV = bax / ad;
                    if (Double.isNaN(bg) || bg < nV) {
                        bg = nV;
                    }
                } else if (ad > 0.0) {
                    double nV = bax / ad;
                    if (Double.isNaN(ed) || ed > nV) {
                        ed = nV;
                    }
                }
            }
        }

        if (Double.isNaN(bg)) {
            throw new RuntimeException("Cannot find begin of segment for given direction. The sampling region is unbounded.");
        }

        if (Double.isNaN(ed)) {
            throw new RuntimeException("Cannot find end of segment for given direction. The sampling region is unbounded.");
        }

        if (bg >= ed) {
            // bg == ed => polytope defined by provided set of inequalities Ax <= b is not full-dimensional
            // bg > ed => something went wrong/method error
            // above cases are considered together due to computer inaccuracy; possible split into two conditions:
            throw new RuntimeException("PolytopeRunner defined by provided set of inequalities is not full-dimensional or method error.");

            // it also fails if sampler will be right in one of the vertices and direction 'd' will not allow to move anywhere; the probability of such situation goes to zero
        }

        if (bg > 0.0) {
            if (bg < 1e-10) {
                // this happens very often when the current point is no the facet of the polytope (e.g., Ball-Walk or SphereWalk with cropping)
                bg = 0.0;
            } else {
                // looks like previous point was outside of sampling region or accuracy error
                throw new RuntimeException("Accuracy or method error (begin of segment).");
            }
        }

        if (ed < 0.0) {
            if (ed > -1e-10) {
                // this happens very often when the current point is no the facet of the polytope (e.g., Ball-Walk or SphereWalk with cropping)
                ed = 0.0;
            } else {
                // looks like previous point was outside of sampling region or accuracy error
                throw new RuntimeException("Accuracy or method error (end of segment).");
            }
        }

        // Select a step size
        double stepLength = (bg + (ed - bg) * this.random.nextDouble());

        for (int i = 0; i < A[0].length; i++) {
            to[i] = buffer[i] * stepLength + from[i];
        }
    }
}
