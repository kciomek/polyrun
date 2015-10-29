package polyrun.constraints;

import org.junit.Assert;
import org.junit.Test;

public class ConstraintsSystemTest {
    private final double accuracy = 1e-10;

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroRowsLhs_throws() throws Exception {
        new ConstraintsSystem(new double[0][3], new String[]{"=", "="}, new double[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroColumnsLhs_throws() throws Exception {
        new ConstraintsSystem(new double[][]{{}, {}}, new String[]{"=", "="}, new double[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_wrongRhsLength_throws() throws Exception {
        new ConstraintsSystem(new double[][]{{1, 0}, {0, 1}}, new String[]{"=", "="}, new double[]{0, 0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_wrongDirLength_throws() throws Exception {
        new ConstraintsSystem(new double[][]{{1, 0}, {0, 1}}, new String[]{"=", "=", "="}, new double[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_doubleEqDir_throws() throws Exception {
        new ConstraintsSystem(new double[][]{{1, 0}, {0, 1}}, new String[]{"<=", "=="}, new double[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ltDir_throws() throws Exception {
        new ConstraintsSystem(new double[][]{{1, 0}, {0, 1}}, new String[]{"<", "="}, new double[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_gtDir_throws() throws Exception {
        new ConstraintsSystem(new double[][]{{1, 0}, {0, 1}}, new String[]{">", "="}, new double[]{0, 0});
    }

    @Test
    public void testConstructor_changingDirection() throws Exception {
        final double[][] lhs = new double[][]{{0, 0, 0}, {1, -1, 1}, {-2, 2, -2}, {3, -3, -3}};
        final String[] dir = new String[]{"<=", ">=", "<=", ">="};
        final double[] rhs = new double[]{0, -1, -2, 3};

        ConstraintsSystem constraintsSystem = new ConstraintsSystem(lhs, dir, rhs);
        double[][] A = constraintsSystem.getA();
        double[] b = constraintsSystem.getB();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (dir[i].equals("<=")) {
                    Assert.assertEquals(lhs[i][j], A[i][j], accuracy);
                } else {
                    Assert.assertEquals(-lhs[i][j], A[i][j], accuracy);
                }
            }

            if (dir[i].equals("<=")) {
                Assert.assertEquals(rhs[i], b[i], accuracy);
            } else {
                Assert.assertEquals(-rhs[i], b[i], accuracy);
            }
        }
    }

    @Test
    public void testConstructor_extractEqDirection() throws Exception {
        final double[][] lhs = new double[][]{{0, 0, 0}, {1, -1, 1}, {-2, 2, -2}, {3, -3, -3}, {4, 4, 4}};
        final String[] dir = new String[]{"<=", "=", "<=", "=", "<="};
        final double[] rhs = new double[]{0, -1, -2, 3, 4};

        ConstraintsSystem constraintsSystem = new ConstraintsSystem(lhs, dir, rhs);
        double[][] A = constraintsSystem.getA();
        double[] b = constraintsSystem.getB();
        double[][] C = constraintsSystem.getC();
        double[] d = constraintsSystem.getD();

        Assert.assertEquals(lhs[0].length, A[0].length);
        Assert.assertEquals(lhs[0].length, C[0].length);
        Assert.assertEquals(lhs.length, A.length + C.length);
        Assert.assertEquals(rhs.length, b.length + d.length);

        Assert.assertEquals(2, C.length);
        Assert.assertEquals(2, d.length);

        Assert.assertArrayEquals(lhs[0], A[0], accuracy);
        Assert.assertArrayEquals(lhs[1], C[0], accuracy);
        Assert.assertArrayEquals(lhs[2], A[1], accuracy);
        Assert.assertArrayEquals(lhs[3], C[1], accuracy);
        Assert.assertArrayEquals(lhs[4], A[2], accuracy);

        Assert.assertEquals(rhs[0], b[0], accuracy);
        Assert.assertEquals(rhs[1], d[0], accuracy);
        Assert.assertEquals(rhs[2], b[1], accuracy);
        Assert.assertEquals(rhs[3], d[1], accuracy);
        Assert.assertEquals(rhs[4], b[2], accuracy);
    }
}