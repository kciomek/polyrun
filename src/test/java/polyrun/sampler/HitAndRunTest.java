package polyrun.sampler;

import org.junit.Assert;
import org.junit.Test;
import polyrun.InteriorPoint;
import polyrun.solver.CommonMathGLPSolverWrapper;
import polyrun.thinning.NoThinning;

public class HitAndRunTest {

    private double sum(double[] vector) {
        double result = 0.0;

        for (int i = 0; i < vector.length; i++) {
            result += vector[i];
        }

        return result;
    }

    @Test
    public void testSample() throws Exception {
        final int numberOfSamples = 10000;

        for (int numberOfDimensions = 1; numberOfDimensions < 100; numberOfDimensions += 5) {
            double[][] A = new double[numberOfDimensions + 1][numberOfDimensions];
            double[] b = new double[numberOfDimensions + 1];

            for (int i = 0; i < numberOfDimensions; i++) {
                A[i][i] = -1.0;
                A[numberOfDimensions][i] = 1.0;
            }

            b[numberOfDimensions] = 1.0;

            double[][] samples = new HitAndRun(new NoThinning()).sample(
                    A, b,
                    false,
                    new InteriorPoint().generate(A, b, new CommonMathGLPSolverWrapper(), false, false),
                    numberOfSamples);
            Assert.assertEquals(numberOfSamples, samples.length);

            for (double[] sample : samples) {
                Assert.assertTrue(sum(sample) <= 1.0);
                for (double variableValue : sample) {
                    Assert.assertTrue(variableValue >= 0.0);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSample_notPositiveNumberOfSamples_throws() throws Exception {
        double[][] A = new double[][]{{-1, 0}, {0, -1}, {1, 1}};
        double[] b = new double[]{0, 0, 1};
        double[] startPoint = new double[]{0, 0};
        new HitAndRun(new NoThinning()).sample(A, b, false, startPoint, 0);
    }

    @Test(expected = IllegalArgumentException.class)
     public void testSample_zeroColumnsMatrixA_throws() throws Exception {
        double[][] A = new double[3][0];
        double[] b = new double[]{0, 0, 1};
        double[] startPoint = new double[]{0, 0};
        new HitAndRun(new NoThinning()).sample(A, b, false, startPoint, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSample_zeroRowsMatrixA_throws() throws Exception {
        double[][] A = new double[0][2];
        double[] b = new double[]{0, 0, 1};
        double[] startPoint = new double[]{0, 0};
        new HitAndRun(new NoThinning()).sample(A, b, false, startPoint, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSample_wrongSizeOfMatrixAAndVectorB_throws() throws Exception {
        double[][] A = new double[][]{{-1, 0}, {0, -1}, {1, 1}};
        double[] b = new double[]{0, 0, 1, 0};
        double[] startPoint = new double[]{0, 0};
        new HitAndRun(new NoThinning()).sample(A, b, false, startPoint, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSample_wrongSizeOfMatrixAAndStartVector_throws() throws Exception {
        double[][] A = new double[][]{{-1, 0}, {0, -1}, {1, 1}};
        double[] b = new double[]{0, 0, 1};
        double[] startPoint = new double[]{0, 0, 0};
        new HitAndRun(new NoThinning()).sample(A, b, false, startPoint, 1);
    }

    @Test
    public void testSample_variableComparison() throws Exception {
        final int numberOfSamples = 100000;

        double[][] A = new double[][]{{-1, 0}, {0, -1}, {1, 0}, {1, 0}, {1, -1}};
        double[] b = new double[]{0, 0, 1, 1, 0};
        double[] startPoint = new double[]{0.5, 0.5};

        double[][] samples = new HitAndRun(new NoThinning()).sample(A, b, false, startPoint, numberOfSamples);
        for (double[] sample : samples){
            Assert.assertTrue(sample[0] <= sample[1]);
        }
    }
}