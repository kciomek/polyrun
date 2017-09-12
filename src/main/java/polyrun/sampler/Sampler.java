package polyrun.sampler;


/**
 * @deprecated This interface will be removed in version 1.0.0. Use classes from package {@link polyrun.sampling} instead.
 */
@Deprecated
public interface Sampler {
    /**
     * Samples from convex polytope defined by linear inequalities A x &le; b.
     * <p>
     * Note: A x &le; b is expected to be consistent and full-dimensional system of inequalities.
     *
     * @param A               lhs coefficients
     * @param b               rhs coefficients
     * @param homogeneous     whether provided system is in homogeneous coordinates
     * @param startPoint      start point
     * @param numberOfSamples number of samples
     * @return samples
     */
    double[][] sample(double[][] A, double[] b, boolean homogeneous, double[] startPoint, int numberOfSamples);
}
