package polyrun.thinning;

public class ConstantThinningFunction implements ThinningFunction {
    private final int constant;

    public ConstantThinningFunction(int constant) {
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
