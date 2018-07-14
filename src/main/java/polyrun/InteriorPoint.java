package polyrun;

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.solver.GLPSolver;
import polyrun.solver.SolverResult;

import java.util.Arrays;
import java.util.Random;

public class InteriorPoint {
    private final Random random;

    public InteriorPoint(Random random) {
        this.random = random;
    }

    public InteriorPoint() {
        this(new Random());
    }

    /**
     * Finds interior point (Chebyshev center) of Ax &le; b. Note that the system is required to be full-dimensional.
     *
     * @param A      matrix A
     * @param b      vector b
     * @param solver solver
     * @return interior point
     * @throws UnboundedSystemException  if unbounded
     * @throws InfeasibleSystemException if not feasible
     */
    public double[] generate(double[][] A, double[] b, GLPSolver solver) throws UnboundedSystemException, InfeasibleSystemException {
        int numberOfOriginalConstrains = A.length;
        int numberOfOriginalVariables = A[0].length;

        // max d
        // s.t.
        // | A I  0 | |x| = b
        //            |e|
        //            |d|
        //
        // | 0 -1 1 | |x| <= 0
        //            |e|
        //            |d|

        double[][] equalitiesLhs = new double[numberOfOriginalConstrains][numberOfOriginalVariables + numberOfOriginalConstrains + 1];
        double[] equalitiesRhs = new double[numberOfOriginalConstrains];
        double[][] inequalitiesLhs = new double[numberOfOriginalConstrains][numberOfOriginalVariables + numberOfOriginalConstrains + 1];
        double[] inequalitiesRhs = new double[numberOfOriginalConstrains];
        double[] objective = new double[numberOfOriginalVariables + numberOfOriginalConstrains + 1];

        for (int i = 0; i < numberOfOriginalConstrains; i++) {
            System.arraycopy(A[i], 0, equalitiesLhs[i], 0, numberOfOriginalVariables);
            for (int j = 0; j < numberOfOriginalConstrains; j++) {
                if (i == j) {
                    equalitiesLhs[i][numberOfOriginalVariables + j] = 1.0;
                }
            }
            equalitiesLhs[i][numberOfOriginalConstrains + numberOfOriginalVariables] = 0.0;
        }

        System.arraycopy(b, 0, equalitiesRhs, 0, numberOfOriginalConstrains);

        for (int i = 0; i < numberOfOriginalConstrains; i++) {
            for (int j = 0; j < numberOfOriginalConstrains; j++) {
                if (i == j) {
                    inequalitiesLhs[i][numberOfOriginalVariables + j] = -1.0;
                }
            }

            inequalitiesLhs[i][numberOfOriginalConstrains + numberOfOriginalVariables] = 1.0;
        }

        // set objective (d)
        objective[numberOfOriginalConstrains + numberOfOriginalVariables] = 1.0;

        SolverResult solverResult = solver.solve(GLPSolver.Direction.Maximize,
                objective,
                new ConstraintsSystem(inequalitiesLhs, inequalitiesRhs,
                        equalitiesLhs, equalitiesRhs));

        if (!solverResult.isFeasible()) {
            throw new InfeasibleSystemException("System is infeasible. It should not happen here.");
        }

        if (solverResult.getValue() <= 0.0) {
            throw new InfeasibleSystemException("Cannot find interior point. The original problem is infeasible or degenerated to a point. Slack = " + solverResult.getValue());
        }

        return Arrays.copyOfRange(solverResult.getSolution(), 0, A[0].length);
    }
}
