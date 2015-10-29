package polyrun.solver;

import org.junit.Assert;
import org.junit.Test;
import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.UnboundedSystemException;

/**
 * Contains a few basic tests for CommonMathGLPSolverWrapper.
 */
public class CommonMathGLPSolverWrapperTest {
    private static final double accuracy = 1e-10;

    @Test
    public void testSolve_maximize_withNonNegativeTarget() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {1, 0},
                        {0, 1}
                },
                new double[]{1, 1}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Maximize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(true, result.isFeasible());
        Assert.assertEquals(2.0, result.getValue(), CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(1.0, result.getSolution()[0], CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(1.0, result.getSolution()[1], CommonMathGLPSolverWrapperTest.accuracy);
    }

    @Test
    public void testSolve_maximize_withNegativeTarget() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {1, 0},
                        {0, 1}
                },
                new double[]{-1, -1}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Maximize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(true, result.isFeasible());
        Assert.assertEquals(-2.0, result.getValue(), CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(-1.0, result.getSolution()[0], CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(-1.0, result.getSolution()[1], CommonMathGLPSolverWrapperTest.accuracy);
    }

    @Test
    public void testSolve_minimize_withNonNegativeTarget() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {-1, 0},
                        {0, -1}
                },
                new double[]{0, 0}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Minimize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(true, result.isFeasible());
        Assert.assertEquals(0.0, result.getValue(), CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(0.0, result.getSolution()[0], CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(0.0, result.getSolution()[1], CommonMathGLPSolverWrapperTest.accuracy);
    }

    @Test
    public void testSolve_minimize_withNegativeTarget() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {-1, 0},
                        {0, -1}
                },
                new double[]{1, 1}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Minimize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(true, result.isFeasible());
        Assert.assertEquals(-2.0, result.getValue(), CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(-1.0, result.getSolution()[0], CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(-1.0, result.getSolution()[1], CommonMathGLPSolverWrapperTest.accuracy);
    }


    @Test
    public void testSolve_infeasibleSolution() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {1, 0},
                        {0, 1},
                        {-1, 0}
                },
                new double[]{1, 1, -2}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Maximize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(false, result.isFeasible());
    }


    @Test(expected = UnboundedSystemException.class)
    public void testSolve_maximize_unbounded_throws() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {1, 0},
                        {0, -1}
                },
                new double[]{1, 1}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        solver.solve(GLPSolver.Direction.Maximize, new double[]{1, 1}, constraintsSystem);
    }


    @Test(expected = UnboundedSystemException.class)
    public void testSolve_minimize_unbounded_throws() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {1, 0},
                        {0, -1}
                },
                new double[]{1, 1}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        solver.solve(GLPSolver.Direction.Minimize, new double[]{1, 1}, constraintsSystem);
    }

    @Test
    public void testSolve_maximize_withEqualitiesAndNonNegativeTarget() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {1, 0},
                        {0, 1}
                },
                new double[]{1, 1},
                new double[][]{
                        {1, 0}
                },
                new double[]{0.3}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Maximize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(true, result.isFeasible());
        Assert.assertEquals(1.3, result.getValue(), CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(0.3, result.getSolution()[0], CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(1.0, result.getSolution()[1], CommonMathGLPSolverWrapperTest.accuracy);
    }

    @Test
    public void testSolve_minimize_withEqualitiesAndNonNegativeTarget() throws Exception {
        ConstraintsSystem constraintsSystem = new ConstraintsSystem(
                new double[][]{
                        {-1, 0},
                        {0, -1}
                },
                new double[]{-0.1, -0.2},
                new double[][]{
                        {1, 0}
                },
                new double[]{0.3}
        );

        GLPSolver solver = new CommonMathGLPSolverWrapper();
        SolverResult result = solver.solve(GLPSolver.Direction.Minimize, new double[]{1, 1}, constraintsSystem);

        Assert.assertEquals(true, result.isFeasible());
        Assert.assertEquals(0.5, result.getValue(), CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(0.3, result.getSolution()[0], CommonMathGLPSolverWrapperTest.accuracy);
        Assert.assertEquals(0.2, result.getSolution()[1], CommonMathGLPSolverWrapperTest.accuracy);
    }
}
