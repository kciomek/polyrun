package polyrun.thinning;

/**
 * Represents constant thinning function in form f(m, n) = c that does not depend on number of dimensions 'n'
 * or number of constraints 'm', and where 'c' is constant.
 */
public class ConstantThinningFunction implements ThinningFunction {
    private final int constant;

    /**
     * @param constant Has to be greater than 0.
     */
    public ConstantThinningFunction(int constant) {
        if (constant <= 0) {
            throw new IllegalArgumentException("Value of 'constant' cannot be equal or less than 0.");
        }

        this.constant = constant;
    }

    @Override
    public int getThinningFactor(int constraints, int dimensions) {
        return this.constant;
    }

    @Override
    public String toString() {
        return "ConstantThinningFunction";
    }
}
