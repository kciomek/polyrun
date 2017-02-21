package polyrun.thinning;

public class ConstantThinningFunction implements ThinningFunction {
    private final int constant;

    public ConstantThinningFunction(int constant) {
        if (constant <= 0) {
            throw new IllegalArgumentException("Value of 'constant' cannot be equal or less than 0.");
        }

        this.constant = constant;
    }

    @Override
    public int getThinningFactor(int dimensions) {
        return this.constant;
    }

    @Override
    public String toString() {
        return "ConstantThinningFunction";
    }
}
