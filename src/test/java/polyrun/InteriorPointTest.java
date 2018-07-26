package polyrun;

import org.junit.Assert;
import org.junit.Test;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.solver.CommonsMathGLPSolverWrapper;

public class InteriorPointTest {
    private static final double accuracy = 1e-9;

    @Test
    public void test_2D_nonRandomized_square() throws Exception {
        final double[][] A = new double[][]{
                {1, 0},
                {0, 1},
                {-1, 0},
                {0, -1}};
        final double[] b = new double[]{1, 1, 0, 0};

        double[] generatedPoint = new InteriorPoint().generate(A, b, new CommonsMathGLPSolverWrapper());

        Assert.assertEquals(A[0].length, generatedPoint.length);
        Assert.assertEquals(0.5, generatedPoint[0], accuracy);
        Assert.assertEquals(0.5, generatedPoint[1], accuracy);
    }

    @Test
    public void test_2D_nonRandomized_rect() throws Exception {
        final double[][] A = new double[][]{
                {1, 0},
                {0, 1},
                {-1, 0},
                {0, -1}};
        final double[] b = new double[]{1, 4, 10, -2};

        double[] generatedPoint = new InteriorPoint().generate(A, b, new CommonsMathGLPSolverWrapper());

        Assert.assertEquals(A[0].length, generatedPoint.length);
        Assert.assertEquals(0.0, generatedPoint[0], accuracy);
        Assert.assertEquals(3.0, generatedPoint[1], accuracy);
    }

    @Test(expected = UnboundedSystemException.class)
    public void test_2D_nonRandomized_unboundedRegion_throws() throws Exception {
        final double[][] A = new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {-1, 0, 1}};
        final double[] b = new double[]{1, 1, 0};

        new InteriorPoint().generate(A, b, new CommonsMathGLPSolverWrapper());
    }

    @Test(expected = InfeasibleSystemException.class)
    public void test_2D_nonRandomized_infeasibleRegion_throws() throws Exception {
        final double[][] A = new double[][]{
                {1, 0},
                {0, 1},
                {-1, -1},
                {1, 0}};
        final double[] b = new double[]{1, 1, 0, -1};

        new InteriorPoint().generate(A, b, new CommonsMathGLPSolverWrapper());
    }
}