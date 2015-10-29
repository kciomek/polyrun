package polyrun;


import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class UnitNSphereTest {
    private final static double accuracy = 1e-10;

    private double sumOfSquares(double[] vector, boolean homogeneous) {
        double result = 0.0;

        int n = homogeneous ? vector.length - 1 : vector.length;

        for (int i = 0; i < n; i++) {
            result += vector[i] * vector[i];
        }

        return result;
    }

    // Incorrect input does not cause an exception

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testFillVectorWithRandomPoint_CaseVectorLen0InHomogeneous_notThrow() {
        // the input is faulty, but we do not expect any action
        double[] vector = new double[0];
        new UnitNSphere().fillVectorWithRandomPoint(vector, true);
    }

    // Empty input does not cause an exception

    @Test
    public void testFillVectorWithRandomPoint_CaseVectorLen0_notThrow() {
        double[] vector = new double[0];
        new UnitNSphere().fillVectorWithRandomPoint(vector, false);
    }

    @Test
    public void testFillVectorWithRandomPoint_CaseVectorLen1InHomogeneous_notThrow() {
        // the case equivalent to testFillVectorWithRandomPoint_CaseVectorLen0_notThrows
        double[] vector = new double[1];
        new UnitNSphere().fillVectorWithRandomPoint(vector, false);
        Assert.assertEquals(1.0, Math.abs(vector[0]), UnitNSphereTest.accuracy);
    }

    // 0-sphere

    @Test
    public void testFillVectorWithRandomPoint_CaseVectorLen1() {
        double[] vector = new double[1];
        new UnitNSphere().fillVectorWithRandomPoint(vector, false);
        Assert.assertEquals(1.0, Math.abs(vector[0]), UnitNSphereTest.accuracy);
    }

    @Test
    public void testFillVectorWithRandomPoint_CaseVectorLen2InHomogeneous() {
        double[] vector = new double[2];
        new UnitNSphere().fillVectorWithRandomPoint(vector, true);
        Assert.assertEquals(1.0, Math.abs(vector[0]), UnitNSphereTest.accuracy);
        Assert.assertEquals(0.0, Math.abs(vector[1]), UnitNSphereTest.accuracy);
    }

    // 1-sphere

    @Test
    public void testFillVectorWithRandomPoint_2sphereRadiusEquals1() {
        UnitNSphere unitNSphere = new UnitNSphere(new Random(0));
        final boolean homogeneous = false;

        double[] vector = new double[2];
        unitNSphere.fillVectorWithRandomPoint(vector, homogeneous);
        Assert.assertEquals(1.0, Math.sqrt(this.sumOfSquares(vector, homogeneous)), UnitNSphereTest.accuracy);
    }

    @Test
    public void testFillVectorWithRandomPoint_2sphereRadiusEquals1InHomogeneous() {
        UnitNSphere unitNSphere = new UnitNSphere(new Random(0));
        final boolean homogeneous = true;

        double[] vector = new double[3];
        unitNSphere.fillVectorWithRandomPoint(vector, homogeneous);
        Assert.assertEquals(1.0, Math.sqrt(this.sumOfSquares(vector, homogeneous)), UnitNSphereTest.accuracy);
        Assert.assertEquals(0.0, vector[2], UnitNSphereTest.accuracy);
    }

    // 99-sphere

    @Test
    public void testFillVectorWithRandomPoint_100sphereRadiusEquals1() {
        UnitNSphere unitNSphere = new UnitNSphere(new Random(0));
        final boolean homogeneous = false;

        double[] vector = new double[100];
        unitNSphere.fillVectorWithRandomPoint(vector, homogeneous);
        Assert.assertEquals(1.0, Math.sqrt(this.sumOfSquares(vector, homogeneous)), UnitNSphereTest.accuracy);
    }

    @Test
    public void testFillVectorWithRandomPoint_100sphereRadiusEquals1InHomogeneous() {
        UnitNSphere unitNSphere = new UnitNSphere(new Random(0));
        final boolean homogeneous = true;

        double[] vector = new double[101];
        unitNSphere.fillVectorWithRandomPoint(vector, homogeneous);
        Assert.assertEquals(1.0, Math.sqrt(this.sumOfSquares(vector, homogeneous)), UnitNSphereTest.accuracy);
        Assert.assertEquals(0.0, vector[100], UnitNSphereTest.accuracy);
    }
}
