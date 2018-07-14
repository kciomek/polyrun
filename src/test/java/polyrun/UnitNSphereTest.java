package polyrun;


import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class UnitNSphereTest {
    private final static double accuracy = 1e-10;

    private double sumOfSquares(double[] vector) {
        double result = 0.0;

        for (int i = 0; i < vector.length; i++) {
            result += vector[i] * vector[i];
        }

        return result;
    }

    // Empty input does not cause an exception

    @Test
    public void testFillVectorWithRandomPoint_CaseVectorLen0_notThrow() {
        double[] vector = new double[0];
        new UnitNSphere().fillVectorWithRandomPoint(vector);
    }

    // 0-sphere

    @Test
    public void testFillVectorWithRandomPoint_CaseVectorLen1_notThrow() {
        double[] vector = new double[1];
        new UnitNSphere().fillVectorWithRandomPoint(vector);
        Assert.assertEquals(1.0, Math.abs(vector[0]), UnitNSphereTest.accuracy);
    }

    // 1-sphere

    @Test
    public void testFillVectorWithRandomPoint_2sphereRadiusEquals1() {
        UnitNSphere unitNSphere = new UnitNSphere(new Random(0));
        double[] vector = new double[2];
        unitNSphere.fillVectorWithRandomPoint(vector);
        Assert.assertEquals(1.0, Math.sqrt(this.sumOfSquares(vector)), UnitNSphereTest.accuracy);
    }

    // 99-sphere

    @Test
    public void testFillVectorWithRandomPoint_99sphereRadiusEquals1() {
        UnitNSphere unitNSphere = new UnitNSphere(new Random(0));

        double[] vector = new double[100];
        unitNSphere.fillVectorWithRandomPoint(vector);
        Assert.assertEquals(1.0, Math.sqrt(this.sumOfSquares(vector)), UnitNSphereTest.accuracy);
    }
}
