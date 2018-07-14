package polyrun.thinning;

/**
 * Represents thinning function in form f(m, n) = ceil(c * m * n), where 'n' is the dimension of sampling space,
 * 'm' is the number of constraints, and 'c' is a parameter (scaling factor).
 */
public class MNThinningFunction implements ThinningFunction {
    private final double scalingFactor;

    /**
     * @param scalingFactor Has to be greater than 0.
     */
    public MNThinningFunction(double scalingFactor) {
        if (scalingFactor <= 0.0) {
            throw new IllegalArgumentException("Value of 'scalingFactor' cannot be equal or less than 0.");
        }

        this.scalingFactor = scalingFactor;
    }

    @Override
    public int getThinningFactor(int constraints, int dimensions) {
        return (int) Math.ceil(this.scalingFactor * constraints * dimensions);
    }

    @Override
    public String toString() {
        return "MNThinningFunction{" + this.scalingFactor + "}";
    }
}
