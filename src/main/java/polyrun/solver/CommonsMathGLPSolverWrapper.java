// Copyright (c) 2015-2018 Krzysztof Ciomek
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package polyrun.solver;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.UnboundedSystemException;

import java.util.ArrayList;
import java.util.Collection;

public class CommonsMathGLPSolverWrapper implements GLPSolver {
    private final SimplexSolver solver;

    public CommonsMathGLPSolverWrapper(SimplexSolver solver) {
        this.solver = solver;
    }

    public CommonsMathGLPSolverWrapper() {
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
