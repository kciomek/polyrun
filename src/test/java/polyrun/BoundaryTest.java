package polyrun;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class BoundaryTest {
    private static final double ASSERT_EPS = 1e-10;

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                { // on boundary on the first axis
                        new double[][]{
                                {-1, 0},
                                {0, -1}
                        },
                        new double[]{0, 0},
                        new double[]{-1, 0},
                        new double[]{0.0, 0.123},
                        1e-10, // eps
                        null,
                        new double[] { 0.0, Double.NEGATIVE_INFINITY }
                },
                { // on boundary on the second axis
                        new double[][]{
                                {-1, 0},
                                {0, -1}
                        },
                        new double[]{0, 0},
                        new double[]{0, -1},
                        new double[]{0.123, 0.0},
                        1e-10, // eps
                        null,
                        new double[] { 0.0, Double.NEGATIVE_INFINITY }
                },
                { // unbounded in direction d towards positive values
                        new double[][]{
                                {-1, 0},
                                {0, -1}
                        },
                        new double[]{0, 0},
                        new double[]{Math.sqrt(0.5), Math.sqrt(0.5)},
                        new double[]{0.2, 0.2},
                        1e-10, // eps
                        null,
                        new double[] { Double.POSITIVE_INFINITY, -Math.sqrt(2 * 0.2 * 0.2) }
                },
                { // unbounded in direction d towards negative values
                        new double[][]{
                                {1, 0},
                                {0, 1}
                        },
                        new double[]{1, 1},
                        new double[]{-Math.sqrt(0.5), -Math.sqrt(0.5)},
                        new double[]{0.2, 0.2},
                        1e-10, // eps
                        null,
                        new double[] { Double.POSITIVE_INFINITY, -Math.sqrt(2 * 0.8 * 0.8) }
                },
                { // unbounded in direction -d towards negative values
                        new double[][]{
                                {-1, 0},
                                {0, -1}
                        },
                        new double[]{0, 0},
                        new double[]{-Math.sqrt(0.5), -Math.sqrt(0.5)},
                        new double[]{0.2, 0.2},
                        1e-10, // eps
                        null,
                        new double[] { Math.sqrt(2 * 0.2 * 0.2), Double.NEGATIVE_INFINITY }
                },
                { // unbounded in direction -d towards positive values
                        new double[][]{
                                {1, 0},
                                {0, 1}
                        },
                        new double[]{1, 1},
                        new double[]{Math.sqrt(0.5), Math.sqrt(0.5)},
                        new double[]{0.2, 0.2},
                        1e-10, // eps
                        null,
                        new double[] { Math.sqrt(2 * 0.8 * 0.8), Double.NEGATIVE_INFINITY }
                },
                { // bounded
                        new double[][]{
                                {-1, 0},
                                {0, -1},
                                {1, 1}
                        },
                        new double[]{0, 0, 1},
                        new double[]{Math.sqrt(0.5), Math.sqrt(0.5)},
                        new double[]{0.3, 0.2},
                        1e-10, // eps
                        null,
                        new double[] {
                                Math.sqrt(Math.pow(0.5, 2) - 2 * Math.pow(0.25, 2)),
                                -Math.sqrt(2 * Math.pow(0.2, 2)),
                        }
                },
                { // x out of bounds (x does not satisfies Ax<=b)
                        new double[][]{
                                {1, 0},
                                {0, 1},
                                {1, 1}
                        },
                        new double[]{1, 1, 1},
                        new double[]{Math.sqrt(0.5), Math.sqrt(0.5)},
                        new double[]{2.0, 2.0},
                        1e-10, // eps
                        RuntimeException.class,
                        null
                },
                { // unbounded but on different (parallel) boundary
                        new double[][]{
                                {1, -3}
                        },
                        new double[]{0.0},
                        new double[]{Math.sqrt(0.75), Math.sqrt(0.25)},
                        new double[]{1.0, 1.0 / 3.0},
                        1e-10, // eps
                        null,
                        new double[] { Double.POSITIVE_INFINITY, 0.0 }
                },
                { // unbounded but on different boundary with computational inaccuracy (no epsilon)
                        new double[][]{
                                {1, -3}
                        },
                        new double[]{0.1},
                        new double[]{Math.sqrt(0.75), Math.sqrt(0.25)},
                        new double[]{1.0, 0.3},
                        0.0, // eps
                        RuntimeException.class,
                        null
                },
                { // unbounded but on different boundary with computational inaccuracy (epsilon applied)
                        new double[][]{
                                {1, -3}
                        },
                        new double[]{0.1},
                        new double[]{Math.sqrt(0.75), Math.sqrt(0.25)},
                        new double[]{1.0, 0.3},
                        1e-10, // eps
                        null,
                        new double[] { Double.POSITIVE_INFINITY, 0.0 }
                },
                { // out of bound by 1e-6
                        new double[][]{
                                {1, -3}
                        },
                        new double[]{0.1},
                        new double[]{Math.sqrt(0.75), Math.sqrt(0.25)},
                        new double[]{1.0, 0.3 - 1e-6},
                        1e-10,
                        RuntimeException.class,
                        null
                },
                { // very little angle
                        new double[][]{
                                {-1, 0},
                                {0, -1}
                        },
                        new double[]{0.0, 0.0},
                        new double[]{-Math.sqrt(0.00001), Math.sqrt(0.99999)},
                        new double[]{1.1, 0.0},
                        1e-10, // eps
                        null,
                        new double[] {
                                Math.sqrt(Math.pow(1.1, 2) + Math.pow(1.1 * Math.sqrt(0.99999) / Math.sqrt(0.00001), 2)),
                                0.0
                        }
                },
                { // parallel but precision error (ad ~= -1e-21)
                        new double[][]{
                                {0.00011, -Math.sqrt(0.25) * 0.00011 / Math.sqrt(0.75)}
                        },
                        new double[]{0.0},
                        new double[]{Math.sqrt(0.25), Math.sqrt(0.75)},
                        new double[]{0.0, 0.0},
                        1e-10, // eps
                        null,
                        new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }
                },
                { // parallel but precision error (ad ~= 1e-18)
                        new double[][]{
                                {0.01, -Math.sqrt(0.4) * 0.01 / Math.sqrt(0.6)}
                        },
                        new double[]{0.0},
                        new double[]{Math.sqrt(0.4), Math.sqrt(0.6)},
                        new double[]{0.0, 0.0},
                        1e-10, // eps
                        null,
                        new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }
                },
        };
        return Arrays.asList(data);
    }

    private double[][] A;
    private double[] b;
    private double[] d;
    private double[] x;
    private double epsilon;
    private Class<? extends Exception> expectedException;
    private double[] expectedResult;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public BoundaryTest(double[][] A, double[] b, double[] d, double[] x, double epsilon,
                        Class<? extends Exception> expectedException, double[] expectedResult) {
        this.A = A;
        this.b = b;
        this.d = d;
        this.x = x;
        this.epsilon = epsilon;
        this.expectedException = expectedException;
        this.expectedResult = expectedResult;
    }

    @Test
    public void test() throws Exception {
        if (expectedException != null) {
            thrown.expect(expectedException);
        }

        double[] distance = new Boundary().distance(A, b, d, x, epsilon, null);
        Assert.assertArrayEquals(expectedResult, distance, ASSERT_EPS);
    }
}