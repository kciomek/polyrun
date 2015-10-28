package polyrun.constraints;

public interface Constraint {
    double[] getLhs();
    String getDirection();
    double getRhs();
}
