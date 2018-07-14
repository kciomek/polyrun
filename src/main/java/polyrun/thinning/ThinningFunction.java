package polyrun.thinning;

/**
 * Represents function q = f(m, n) which defines thinning factor q depending on the dimensionality n of sampling space
 * and number of constraints m.
 */
public interface ThinningFunction {

    /**
     * Returns thinning factor for given number of dimensions.
     *
     * @param dimensions number of constraints that defines sampling space
     * @param dimensions number of dimensions of sampling space
     * @return thinning factor
     */
    int getThinningFactor(int constraints, int dimensions);
}
