package polyrun.solver;


public class SolverResult {
    private final boolean feasible;
    private final double value;
    private final double[] solution;

    public SolverResult(boolean feasible, double value, double[] solution) {
        this.feasible = feasible;
        this.value = value;
        this.solution = solution;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public double getValue() {
        return value;
    }

    public double[] getSolution() {
        return solution;
    }
}
