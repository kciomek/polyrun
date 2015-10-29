package polyrun;

import org.junit.Assert;
import org.junit.Test;

public class TransformationTest {
    private static final double accuracy = 1e-7;

    @Test
    public void test_constructTransformationMatrix_simpleCase_1() {
        double[][] C = new double[][]{{1, 0, 1}, {0, 1, 0}};
        double[] d = new double[]{1, 1};
        double[][] t = new Transformation(C, d, 3).getTransformationMatrix();
        Assert.assertArrayEquals(new double[]{-0.7071068, 0.5}, t[0], accuracy);
        Assert.assertArrayEquals(new double[]{0.0, 1.0}, t[1], accuracy);
        Assert.assertArrayEquals(new double[]{0.7071068, 0.5}, t[2], accuracy);
    }

    @Test
    public void test_constructTransformationMatrix_simpleCase_2() {
        double[][] C = new double[][]{{1, 1, 0}, {1, 0, 1}};
        double[] d = new double[]{1, 1.5};
        double[][] t = new Transformation(C, d, 3).getTransformationMatrix();
        Assert.assertArrayEquals(new double[]{-0.5773503, 0.8333333}, t[0], accuracy);
        Assert.assertArrayEquals(new double[]{0.5773503, 0.1666667}, t[1], accuracy);
        Assert.assertArrayEquals(new double[]{0.5773503, 0.6666667}, t[2], accuracy);
    }

    @Test
    public void test_transformationMatrix_emptyC() {
        double[][] C = new double[0][];
        double[] d = new double[0];

        Transformation transformation = new Transformation(C, d, 3);
        double[][] t = transformation.getTransformationMatrix();

        Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0, 0.0}, t[0], accuracy);
        Assert.assertArrayEquals(new double[]{0.0, 1.0, 0.0, 0.0}, t[1], accuracy);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0}, t[2], accuracy);

        Assert.assertArrayEquals(new double[]{0.0, 0.0, 0.0}, transformation.getTranslationVector(), accuracy);
    }

    @Test
    public void test_transformationMatrix_fullC() {
        double[][] C = new double[][]{{0.7, 0, 0}, {0, -2, 0}, {0, 0, 12.3}};
        double[] d = new double[]{0.7, -4, 36.9};

        Transformation transformation = new Transformation(C, d, 3);
        double[][] t = transformation.getTransformationMatrix();

        Assert.assertArrayEquals(new double[]{1.0}, t[0], accuracy);
        Assert.assertArrayEquals(new double[]{2.0}, t[1], accuracy);
        Assert.assertArrayEquals(new double[]{3.0}, t[2], accuracy);

        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, transformation.getTranslationVector(), accuracy);
    }
}
