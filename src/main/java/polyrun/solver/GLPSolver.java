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

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.UnboundedSystemException;

/**
 * Solver of General Linear Programming problem.
 */
public interface GLPSolver {
    enum Direction {
        Maximize,
        Minimize
    }

    /**
     * Solve General Linear Programing Problem (variables are allowed to be negative) by optimizing the 'objective' function
     * under given 'constraints'.
     *
     * @param direction   optimization direction
     * @param objective   coefficients of the objective function
     * @param constraints constraints
     * @return the result of optimization
     * @throws UnboundedSystemException when constraint system is unbounded and solution cannot be found
     */
    SolverResult solve(Direction direction, double[] objective, ConstraintsSystem constraints) throws UnboundedSystemException;
}
