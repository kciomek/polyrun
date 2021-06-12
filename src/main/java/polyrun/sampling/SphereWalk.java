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
        super(random, radius, outOfBoundsBehaviour, 1e-10);
    }

    /**
     * @param random               random number generator
     * @param radius               radius of a sphere
     * @param outOfBoundsBehaviour the behaviour of random walk when it tries to make a step that exceeds bounds of the polytope
     * @param eps                  absolute error to accept in floating point comparisons (non-negative, default 1e-10)
     */
    public SphereWalk(Random random, double radius, OutOfBoundsBehaviour outOfBoundsBehaviour, double eps) {
        super(random, radius, outOfBoundsBehaviour, eps);
    }

    @Override
    protected double getStepLength(double r, int n) {
        // Just return the radius r
        return r;
    }
}
