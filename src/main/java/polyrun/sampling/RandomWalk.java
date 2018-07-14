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
