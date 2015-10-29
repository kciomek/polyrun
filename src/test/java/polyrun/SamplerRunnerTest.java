package polyrun;

import org.junit.Assert;
import org.junit.Test;
import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.sampler.HitAndRun;
import polyrun.solver.CommonMathGLPSolverWrapper;
import polyrun.thinning.NoThinning;

import java.util.Random;

public class SamplerRunnerTest {
    private static final double accuracy = 1e-10;

    @Test
    public void test_sample_hitandrunIsWrappedCorrectly() throws Exception {
        final int seed = 0;
        SamplerRunner runner = new SamplerRunner(new HitAndRun(new NoThinning(), new Random(seed)));
        HitAndRun har = new HitAndRun(new NoThinning(), new Random(seed));

        final int numberOfSamples = 100;

        final double[][] lhs = new double[][]{
                {1, 0},
                {0, 1},
                {1, 1},
        };
        final String[] dir = new String[]{">=", ">=", "<="};
        final double[] rhs = new double[]{0, 0, 1};

        ConstraintsSystem constraintsSystem = new ConstraintsSystem(lhs, dir, rhs);

        double[][] samplesFromWrapper = runner.sample(constraintsSystem, numberOfSamples);
        double[][] samplesDirectlyFromHar = har.sample(
                constraintsSystem.getA(),
                constraintsSystem.getB(),
                false,
                new InteriorPoint().generate(constraintsSystem.getA(),
                        constraintsSystem.getB(),
                        new CommonMathGLPSolverWrapper(),
                        false, false),
                numberOfSamples
        );

        for (int i = 0; i < numberOfSamples; i++) {
            Assert.assertArrayEquals(samplesDirectlyFromHar[i], samplesFromWrapper[i], SamplerRunnerTest.accuracy);
        }
    }

    @Test
    public void test_sample_hitandrunWithOnlyFullEqualities() throws Exception {
        SamplerRunner runner = new SamplerRunner(new HitAndRun(new NoThinning()));
        int numberOfSamples = 1000;

        final double[][] lhs = new double[][]{
                {1, 0},
                {0, 1}
        };
        final String[] dir = new String[]{"=", "="};
        final double[] rhs = new double[]{1, 2};

        double[][] samples = runner.sample(new ConstraintsSystem(lhs, dir, rhs), numberOfSamples);
        for (double[] sample : samples) {
            Assert.assertArrayEquals(new double[]{1, 2}, sample, SamplerRunnerTest.accuracy);
        }
    }

    @Test(expected = UnboundedSystemException.class)
    public void test_sample_hitandrunWithUnboundedRegion() throws Exception {
        SamplerRunner runner = new SamplerRunner(new HitAndRun(new NoThinning()));

        final double[][] lhs = new double[][]{
                {1, 0},
                {0, 1}
        };
        final String[] dir = new String[]{">=", ">="};
        final double[] rhs = new double[]{0, 0};

        runner.sample(new ConstraintsSystem(lhs, dir, rhs), 1);
    }

    @Test(expected = InfeasibleSystemException.class)
    public void test_sample_hitandrunWithInfeasibleRegion() throws Exception {
        SamplerRunner runner = new SamplerRunner(new HitAndRun(new NoThinning()));

        final double[][] lhs = new double[][]{
                {1, 0},
                {0, 1},
                {1, 0},
        };
        final String[] dir = new String[]{">=", ">=", "<="};
        final double[] rhs = new double[]{0, 0, -1};

        runner.sample(new ConstraintsSystem(lhs, dir, rhs), 1);
    }

    @Test
    public void test_sample_hitandrunWithOneIndependentVariable() throws Exception {
        SamplerRunner runner = new SamplerRunner(new HitAndRun(new NoThinning()));

        final double[][] lhs = new double[][]{
                {1, 0, 0},
                {0, 0, 1},
                {1, 0, 1},
                {0, 1, 0},
        };
        final String[] dir = new String[]{">=", ">=", "<=", "="};
        final double[] rhs = new double[]{0, 0, 1, 1};

        double[][] samples = runner.sample(new ConstraintsSystem(lhs, dir, rhs), 100);

        for (double[] sample : samples) {
            Assert.assertEquals(1.0, sample[1], SamplerRunnerTest.accuracy);
        }
    }
}