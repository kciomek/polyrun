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

/**
 * Represents random walk. Provides method to sample from convex polytope described by a system of linear inequalities Ax &le; b.
 */
public interface RandomWalk {

    /**
     * Generates next sample (makes a step in a walk) from a polytope defined by linear inequalities A x &le; b.
     * It starts in {@code from} and fills array {@code to} with generated sample. Note, that {@code to} may be
     * the same reference as {@code from} if overwriting is needed.
     *
     * @param A                           lhs coefficients
     * @param indicesOfNonZeroElementsInA array of array of indices of non-zero elements in A (if not provided
     *                                    the method will iterate over all elements in each row of A; use only if A is
     *                                    relatively sparse; RandomWalk implementation does not have to use it)
     * @param b                           rhs coefficients
     * @param buffer                      buffer (required to be of length of {@code A[0]})
     * @param from                        start point
     * @param to                          point to be filled by a method (required to be of length of {@code from});
     *                                    it may be the same reference as {@code from} if overwriting is needed
     */
    void next(double[][] A, int[][] indicesOfNonZeroElementsInA,
              double[] b, double[] buffer,
              double[] from, double[] to);
}
