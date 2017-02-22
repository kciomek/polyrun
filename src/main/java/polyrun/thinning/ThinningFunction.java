package polyrun.thinning;

/**
 * Represents function q = f(n) which defines thinning factor q depending on the dimensionality n of sampling space.
 */
public interface ThinningFunction {

    /**
     * Returns thinning factor for given number of dimensions.
     *
     * @param dimensions number of dimensions of sampling space
     * @return thinning factor
     */
    int getThinningFactor(int dimensions);
}
