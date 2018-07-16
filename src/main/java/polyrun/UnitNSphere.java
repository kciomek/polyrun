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

package polyrun;

import java.util.Random;

/**
 * Represents n-sphere of unit radius centered at the origin.
 */
public class UnitNSphere {
    private final Random random;

    public UnitNSphere() {
        this.random = new Random();
    }

    public UnitNSphere(Random random) {
        this.random = random;
    }

    /**
     * Simple method of picking random point from unit hyper-sphere. It fills the vector (double[]) passed in parameter.
     * <p>
     * The method generates n random values from normal distribution of mean = 0 and standard deviation = 1
     * and normalize them by square root of sum of their squares.
     *
     * @param vectorToFill vector to fill
     */
    public void fillVectorWithRandomPoint(double[] vectorToFill) {
        double s = 0.0;
        int n = vectorToFill.length;

        for (int i = 0; i < n; i++) {
            vectorToFill[i] = random.nextGaussian();
            s += vectorToFill[i] * vectorToFill[i];
        }

        s = 1.0 / Math.sqrt(s);

        for (int i = 0; i < n; i++) {
            vectorToFill[i] *= s;
        }
    }
}
