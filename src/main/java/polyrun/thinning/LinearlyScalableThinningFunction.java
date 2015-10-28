package polyrun.thinning;

/**
 * Represents thinning function in form f(n) = ceil(a * n^3), where 'n' is the dimension of sampling space,
 * and 'a' is a parameter (scaling factor).
 */
public class LinearlyScalableThinningFunction implements ThinningFunction {
    private final double scalingFactor;

    /**
     * @param scalingFactor Has to be greater than 0.
     */
    public LinearlyScalableThinningFunction(double scalingFactor) {
        if (scalingFactor <= 0.0) {
            throw new IllegalArgumentException("Value of 'scalingFactor' cannot be equal or less than 0.");
        }

        this.scalingFactor = scalingFactor;
    }

    @Override
    public int getThinningFactor(int dimensions) {
        return (int) Math.ceil(this.scalingFactor * Math.pow(dimensions, 3.0));
    }

    @Override
    public String toString() {
        return "LinearlyScalableThinningFunction{" + this.scalingFactor + "}";
    }
}
