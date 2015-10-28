package polyrun.sampler;

public interface Sampler {
    /**
     * Samples from convex polytope defined by linear inequalities A x <= b.
     *
     * Note: A x <= b is expected to be consistent and full-dimensional system of inequalities.
     *
     * @param A
     * @param b
     * @param homogeneous
     * @param startPoint
     * @param numberOfSamples
     * @return
     */
    double[][] sample(double[][] A, double[] b, boolean homogeneous, double[] startPoint, int numberOfSamples);
}
