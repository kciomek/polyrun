package polyrun.solver;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.UnboundedSystemException;

import java.util.ArrayList;
import java.util.Collection;

public class CommonMathGLPSolverWrapper implements GLPSolver {
    private final SimplexSolver solver;

    public CommonMathGLPSolverWrapper(SimplexSolver solver) {
        this.solver = solver;
    }

    public CommonMathGLPSolverWrapper() {
        this.solver = new SimplexSolver();
    }

    @Override
    public SolverResult solve(Direction direction, double[] objective, ConstraintsSystem constraints) throws UnboundedSystemException {
        LinearObjectiveFunction cmObjective = new LinearObjectiveFunction(objective, 0);
        Collection<LinearConstraint> cmConstraints = new ArrayList<LinearConstraint>();

        for (int i = 0; i < constraints.getA().length; i++) {
            cmConstraints.add(new LinearConstraint(constraints.getA()[i], Relationship.LEQ, constraints.getB()[i]));
        }

        for (int i = 0; i < constraints.getC().length; i++) {
            cmConstraints.add(new LinearConstraint(constraints.getC()[i], Relationship.EQ, constraints.getD()[i]));
        }

        GoalType goalType;
        if (direction.equals(Direction.Maximize)) {
            goalType = GoalType.MAXIMIZE;
        } else {
            goalType = GoalType.MINIMIZE;
        }

        try {
            PointValuePair solution = this.solver.optimize(cmObjective,
                    new LinearConstraintSet(cmConstraints),
                    goalType,
                    new NonNegativeConstraint(false));
            return new SolverResult(true, solution.getValue(), solution.getPoint());
        } catch (NoFeasibleSolutionException e) {
            return new SolverResult(false, 0.0, null);
        } catch (UnboundedSolutionException e) {
            throw new UnboundedSystemException(e);
        }
    }
}
