package polyrun.thinning;

/**
 * Represents thinning function in form f(n) = 1.
 */
public class NoThinning extends ConstantThinningFunction {
    public NoThinning() {
        super(1);
    }

    @Override
    public String toString() {
        return "NoThinning";
    }
}
