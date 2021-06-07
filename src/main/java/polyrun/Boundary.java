package polyrun;


public class Boundary {

    /**
     * Calculates distance from point x to a boundary of a polytope defined by Ax ≤ b
     * in directions d and -d.
     * <p>
     * The method calculates the minimum value (for direction d, and maximum for -d) of (b-Ax)_i/((Ad)_i) over
     * all i such that 0 &lt; i &lt; m and (Ad)_i &gt; 0,
     * where m is the number of inequalities (rows in matrix A) and (v)_i is the i-th element of vector v.
     *
     * @param A   matrix
     * @param b   vector
     * @param d   direction
     * @param x   current point (vector)
     * @param eps absolute error to allow (non-negative), i.e., the greatest value treated as 0;
     *            applied also to check Ax ≤ b
     * @param indicesOfNonZeroElementsInA array of array of indices of non-zero elements in A (if not provided
     *            the method will iterate over all elements in each row of A; use only if A is relatively sparse)
     * @return two element array with distances to the boundary of the polytope from given point x;
     *         the first value is non-negative and represents distance in direction d,
     *         and the second is non-positive and represents distance backwards (direction -d)
     */
    public final double[] distance(double[][] A, double[] b, double[] d, double[] x, double eps, int[][] indicesOfNonZeroElementsInA) {
        final boolean iterateOverSelectedIndices = indicesOfNonZeroElementsInA != null;

        double[] result = new double[2];
        result[0] = Double.POSITIVE_INFINITY;
        result[1] = Double.NEGATIVE_INFINITY;

        for (int j = 0; j < b.length; j++) {
            double ad = 0.0;
            double bax = b[j];

            if(iterateOverSelectedIndices) {
                for (int i : indicesOfNonZeroElementsInA[j]) {
                    ad += A[j][i] * d[i];
                    bax -= A[j][i] * x[i];
                }
            } else {
                for (int i = 0; i < A[0].length; i++) {
                    ad += A[j][i] * d[i];
                    bax -= A[j][i] * x[i];
                }
            }

            if (-eps <= bax && bax <= eps) {
                bax = 0.0d;
            } else if (bax < 0.0d) {
                throw new RuntimeException("Passed 'x' is out of bounds, i.e., Ax<=b is not satisfied");
            }

            if (ad > eps) {
                result[0] = Math.min(result[0], bax / ad);
            } else if (ad < -eps) {
                result[1] = Math.max(result[1], bax / ad);
            }
        }

        return result;
    }
}
