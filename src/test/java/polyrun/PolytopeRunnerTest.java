package polyrun;

import org.junit.Test;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampling.HitAndRun;
import polyrun.thinning.NoThinning;

import static org.junit.Assert.*;


public class PolytopeRunnerTest {

    private final double accuracy = 1e-9;

    private ConstraintsSystem generateConstraintsSystem() {
        final double[][] lhs = new double[][]{
                {1, 0},
                {0, 1},
                {1, 1}
        };
        final String[] dir = new String[]{">=", ">=", "<="};
        final double[] rhs = new double[]{0, 0, 1};

        return new ConstraintsSystem(lhs, dir, rhs);
    }

    @Test
    public void setAnyStartPoint() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        assertNull(polytopeRunner.getStartPoint());

        polytopeRunner.setAnyStartPoint();

        double[] point = polytopeRunner.getStartPoint();
        assertNotNull(point);
        assertEquals(2, point.length);
        assertTrue(point[0] >= 0);
        assertTrue(point[1] >= 0);
        assertTrue(point[0] + point[1] <= 1);
    }

    @Test
    public void setStartPoint() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        assertNull(polytopeRunner.getStartPoint());

        double[] setPoint = new double[]{0.4, 0.5};
        polytopeRunner.setStartPoint(setPoint);

        double[] point = polytopeRunner.getStartPoint();
        assertNotNull(point);
        assertEquals(2, point.length);
        assertEquals(setPoint[0], point[0], accuracy);
        assertEquals(setPoint[1], point[1], accuracy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStartPointThrowsIfNotInteriorPoint() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        assertNull(polytopeRunner.getStartPoint());

        double[] setPoint = new double[]{0.6, 0.5};
        polytopeRunner.setStartPoint(setPoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStartPointThrowsIfPointTooLong() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        assertNull(polytopeRunner.getStartPoint());

        double[] setPoint = new double[]{0.6, 0.5, 0.4};
        polytopeRunner.setStartPoint(setPoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStartPointThrowsIfPointTooShort() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        assertNull(polytopeRunner.getStartPoint());

        double[] setPoint = new double[]{0.6};
        polytopeRunner.setStartPoint(setPoint);
    }

    @Test
    public void neighborhoodDoesNotChangeStartPoint() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        double[] point = new double[]{0.6, 0.3};
        polytopeRunner.setStartPoint(point);
        polytopeRunner.neighborhood(new HitAndRun(), 100);
        assertArrayEquals(point, polytopeRunner.getStartPoint(), accuracy);
    }

    @Test
    public void neighborhoodWithSampleConsumerDoesNotChangeStartPoint() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        double[] point = new double[]{0.6, 0.3};
        polytopeRunner.setStartPoint(point);
        polytopeRunner.neighborhood(new HitAndRun(), 100, new SampleConsumer() {
            @Override
            public void consume(double[] sample) {
            }
        });
        assertArrayEquals(point, polytopeRunner.getStartPoint(), accuracy);
    }

    @Test(expected = RuntimeException.class)
    public void neighborhoodThrowsIfStartPointNotSet() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        polytopeRunner.neighborhood(new HitAndRun(), 100);
    }

    @Test(expected = RuntimeException.class)
    public void chainThrowsIfStartPointNotSet() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        polytopeRunner.chain(new HitAndRun(), new NoThinning(), 100);
    }

    @Test
    public void neighborhoodReturnsRequestedNumberOfSamplesOfAppropriateLength() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        polytopeRunner.setAnyStartPoint();
        double[][] samples = polytopeRunner.neighborhood(new HitAndRun(), 123);
        assertEquals(123, samples.length);

        for (double[] sample : samples) {
            assertEquals(2, sample.length);
        }
    }

    @Test
    public void chainReturnsRequestedNumberOfSamplesOfAppropriateLength() throws Exception {
        PolytopeRunner polytopeRunner = new PolytopeRunner(this.generateConstraintsSystem());
        polytopeRunner.setAnyStartPoint();
        double[][] samples = polytopeRunner.chain(new HitAndRun(), new NoThinning(), 123);
        assertEquals(123, samples.length);

        for (double[] sample : samples) {
            assertEquals(2, sample.length);
        }
    }
}