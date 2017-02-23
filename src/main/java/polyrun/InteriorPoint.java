package polyrun;

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.solver.GLPSolver;
import polyrun.solver.SolverResult;

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
     * Finds interior point of Ax &le; b. Note that the system is required to be full-dimensional.
     *
     * @param A matrix A
     * @param b vector b
     * @param solver solver
     * @param randomizePoint whether to randomize start point (not uniformly)
     * @param homogeneous whether provided system is in homogeneous coordinates
     * @return interior point
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public double[] generate(double[][] A, double[] b, GLPSolver solver, boolean randomizePoint, boolean homogeneous) throws UnboundedSystemException, InfeasibleSystemException {
        int numberOfOriginalConstrains = A.length;
        int numberOfOriginalVariables = A[0].length;

        double[] result = new double[numberOfOriginalVariables];

        // max d
        // s.t.
        // | A I  0 | |x| = b
        //            |e|
        //            |d|
        // if homogeneous:
        //     | w  0 0 | | | = 1
        //                |e|
        //                |d|, where w = [0..0 1]
        //
        // | 0 -c 1 | |x| <= 0
        //            |e|
        //            |d|

        int numberOfEqualities;

        if (homogeneous) {
            numberOfEqualities = numberOfOriginalConstrains + 1;
        } else {
            numberOfEqualities = numberOfOriginalConstrains;
        }

        double[][] equalitiesLhs = new double[numberOfEqualities][numberOfOriginalVariables + numberOfOriginalConstrains + 1];
        double[] equalitiesRhs = new double[numberOfEqualities];
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

        if (homogeneous) {
            equalitiesLhs[numberOfEqualities - 1][numberOfOriginalVariables - 1] = 1.0;
            equalitiesRhs[numberOfEqualities - 1] = 1.0;
        }

        // randomization
        double[] c = new double[numberOfOriginalConstrains];

        if (randomizePoint) {
            for (int i = 0; i < c.length; i++) {
                c[i] = 1.0 - this.random.nextDouble();// because c[i] has to be in (0.0, 1.0>
            }
        } else {
            for (int i = 0; i < c.length; i++) {
                c[i] = 1.0;
            }
        }

        for (int i = 0; i < numberOfOriginalConstrains; i++) {
            for (int j = 0; j < numberOfOriginalConstrains; j++) {
                if (i == j) {
                    inequalitiesLhs[i][numberOfOriginalVariables + j] = -c[j];
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

        System.arraycopy(solverResult.getSolution(), 0, result, 0, result.length);

        if (homogeneous) {
            result[result.length - 1] = 1.0;
        }

        return result;
    }
}
