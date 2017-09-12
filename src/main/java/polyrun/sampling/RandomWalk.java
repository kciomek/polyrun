package polyrun.sampling;

/**
 * Represents abstract random walk. Provides method to sample from convex polytope described by a system of linear inequalities Ax &le; b.
 */
public abstract class RandomWalk {

    /**
     * Generates next sample (makes a step in a walk) from a polytope defined by linear inequalities A x &le; b.
     * It starts in {@code from} and fills array {@code to} with generated sample. Note, that {@code to} may be
     * the same reference as {@code from} if overwriting is needed.
     *
     * @param A               lhs coefficients
     * @param b               rhs coefficients
     * @param homogeneous     whether provided system is in homogeneous coordinates
     * @param adBuffer        buffer (required to be of length of {@code b})
     * @param baxBuffer       buffer (required to be of length of {@code b})
     * @param directionBuffer buffer (required to be of length of {@code A[0]})
     * @param from            start point
     * @param to              point to be filled by a method (required to be of length of {@code from});
     *                        it may be the same reference as {@code from} if overwriting is needed
     */
    public void next(double[][] A, double[] b, boolean homogeneous,
                     double[] adBuffer, double[] baxBuffer, double[] directionBuffer,
                     double[] from, double[] to) {
        // Generate direction
        this.fillDirectionVector(directionBuffer, homogeneous);

        // Calculate begin and end of the segment along the generated direction
        double bg = Double.NaN;
        double ed = Double.NaN;

        for (int j = 0; j < b.length; j++) {
            adBuffer[j] = 0.0;
            for (int i = 0; i < A[0].length; i++) {
                adBuffer[j] += A[j][i] * directionBuffer[i];
            }
        }

        for (int j = 0; j < b.length; j++) {
            baxBuffer[j] = b[j];
            for (int i = 0; i < A[0].length; i++) {
                baxBuffer[j] -= A[j][i] * from[i];
            }
        }

        for (int j = 0; j < b.length; j++) {
            if (adBuffer[j] < 0.0) {
                double nV = baxBuffer[j] / adBuffer[j];
                if (Double.isNaN(bg) || bg < nV) {
                    bg = nV;
                }
            } else if (adBuffer[j] > 0.0) {
                double nV = baxBuffer[j] / adBuffer[j];
                if (Double.isNaN(ed) || ed > nV) {
                    ed = nV;
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
            // looks like previous point was outside of sampling region or accuracy error
            throw new RuntimeException("Accuracy or method error (begin of segment).");
        }

        if (ed < 0.0) {
            // looks like previous point was outside of sampling region or accuracy error
            throw new RuntimeException("Accuracy or method error (end of segment).");
        }

        // Select a step size
        double stepLength = this.selectStepLength(directionBuffer, bg, ed);

        for (int i = 0; i < A[0].length; i++) {
            to[i] = directionBuffer[i] * stepLength + from[i];
        }
    }

    /**
     * Fills vector with a direction.
     *
     * @param direction   vector to fill
     * @param homogeneous whether provided vector is in homogeneous coordinates
     */
    protected abstract void fillDirectionVector(double[] direction, boolean homogeneous);

    /**
     * Selects length of next step (see
     * {@link RandomWalk#next(double[][], double[], boolean, double[], double[], double[], double[], double[])},
     * where {@code to = directionBuffer * stepLength + from}).
     *
     * @param direction direction of next step
     * @param bg        maximal step backward (non-positive)
     * @param ed        maximal step forward (non-negative)
     * @return step length
     */
    protected abstract double selectStepLength(double[] direction, double bg, double ed);
}
