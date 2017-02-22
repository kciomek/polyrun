package polyrun.constraints;

/**
 * Linear constraint.
 */
public interface Constraint {
    double[] getLhs();
    String getDirection();
    double getRhs();
}
