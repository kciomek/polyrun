package polyrun;

import org.junit.Assert;
import org.junit.Test;

public class TransformationTest {
    private static final double accuracy = 1e-10;

    @Test
    public void nullInput() {
        Transformation t = new Transformation(null, null, 3);
        double[] input = new double[]{-0.1, 0.0, 0.9};
        double[] output = t.project(new double[][]{input})[0];

        Assert.assertArrayEquals(input, output, accuracy);
    }

    @Test
    public void emptyInput() {
        Transformation t = new Transformation(new double[0][0], new double[0], 3);
        double[] input = new double[]{-0.1, 0.0, 0.9};
        double[] output = t.project(new double[][]{input})[0];

        Assert.assertArrayEquals(input, output, accuracy);
    }

    @Test(expected = RuntimeException.class)
    public void exactlyOneSolutionThrows() {
        double[][] C = new double[][]{{1, 0}, {0, 1}};
        double[] d = new double[]{2, 3};

        new Transformation(C, d, 2);
    }

    @Test
    public void moreVariablesThanEquations() {
        double[][] A = new double[][]{
                new double[]{1.0, 0.0, 0.0},
                new double[]{0.0, 1.0, 0.0},
                new double[]{0.0, 0.0, 1.0},
                new double[]{-1.0, 0.0, 0.0},
                new double[]{0.0, -1.0, 0.0},
                new double[]{0.0, 0.0, -1.0},
        };
        double[] b = new double[]{1.0, 1.0, 1.0, 0.0, 0.0, 0.0};
        double[][] C = new double[][]{{1, 0, 1}, {0, 1, 0}};
        double[] d = new double[]{1, 1};

        Transformation t = new Transformation(C, d, 3);

        double[][] Q = t.project(A);
        double[] q = t.solveForParticularSolution(A, b);

        Assert.assertEquals(A.length, Q.length);
        Assert.assertEquals(1, Q[0].length);
        Assert.assertArrayEquals(new double[]{0.5, 0.0, 0.5, 0.5, 1.0, 0.5}, q, accuracy);
        Assert.assertArrayEquals(new double[]{-0.7071067811865475}, Q[0], accuracy);

        double[] s1 = t.projectBack(new double[]{-0.7071067811865475});
        double[] s2 = t.projectBack(new double[]{0.0});
        double[] s3 = t.projectBack(new double[]{0.7071067811865475});
        Assert.assertArrayEquals(new double[]{1.0, 1.0, 0.0}, s1, accuracy);
        Assert.assertArrayEquals(new double[]{0.5, 1.0, 0.5}, s2, accuracy);
        Assert.assertArrayEquals(new double[]{0.0, 1.0, 1.0}, s3, accuracy);
    }
}
