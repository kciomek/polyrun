package polyrun;

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.sampler.Sampler;
import polyrun.solver.CommonMathGLPSolverWrapper;

import java.util.Random;

public class SamplerRunner {
    private final Sampler sampler;
    private final Random random;

    public SamplerRunner(Sampler sampler, Random random) {
        this.sampler = sampler;
        this.random = random;
    }

    public SamplerRunner(Sampler sampler) {
        this(sampler, new Random());
    }

    /**
     *
     * @param constraintsSystem System of linear constraints (Ax<=b, Cx=d). It is expected to be consistent and A is expected to be full-dimensional.
     * @param numberOfSamples
     * @return
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public double[][] sample(ConstraintsSystem constraintsSystem, int numberOfSamples) throws UnboundedSystemException, InfeasibleSystemException {
        return this.runSampler(constraintsSystem, numberOfSamples, false, null);
    }

    /**
     *
     * @param constraintsSystem System of linear constraints (Ax<=b, Cx=d). It is expected to be consistent and A is expected to be full-dimensional.
     * @param numberOfSamples
     * @param startFromRandomizedPoint
     * @return
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public double[][] sample(ConstraintsSystem constraintsSystem, int numberOfSamples, boolean startFromRandomizedPoint) throws UnboundedSystemException, InfeasibleSystemException {
        return this.runSampler(constraintsSystem, numberOfSamples, startFromRandomizedPoint, null);
    }

    /**
     *
     * @param constraintsSystem System of linear constraints (Ax<=b, Cx=d). It is expected to be consistent and A is expected to be full-dimensional.
     * @param numberOfSamples
     * @param consumer
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public void sample(ConstraintsSystem constraintsSystem, int numberOfSamples, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        this.runSampler(constraintsSystem, numberOfSamples, false, consumer);
    }

    /**
     *
     * @param constraintsSystem System of linear constraints (Ax<=b, Cx=d). It is expected to be consistent and A is expected to be full-dimensional.
     * @param numberOfSamples
     * @param startFromRandomizedPoint
     * @param consumer
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public void sample(ConstraintsSystem constraintsSystem, int numberOfSamples, boolean startFromRandomizedPoint, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        this.runSampler(constraintsSystem, numberOfSamples, startFromRandomizedPoint, consumer);
    }

    private double[][] runSampler(ConstraintsSystem constraintsSystem, int numberOfSamples, boolean startFromRandomizedPoint, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        Transformation transformation = new Transformation(constraintsSystem.getC(), constraintsSystem.getD(), constraintsSystem.getNumberOfVariables());
        double[][] samples = null;

        if (transformation.getTransformationMatrix()[0].length == 1) {
            if (consumer == null) {
                samples = new double[numberOfSamples][constraintsSystem.getNumberOfVariables()];

                for (int i = 0; i < numberOfSamples; i++) {
                    System.arraycopy(transformation.getTranslationVector(), 0, samples[i], 0, constraintsSystem.getNumberOfVariables());
                }
            } else {
                for (int i = 0; i < numberOfSamples; i++) {
                    consumer.consume(transformation.getTranslationVector());
                }
            }
        } else {
            double[][] transformedInequalitiesLhs = transformation.reduceDimensionality(constraintsSystem.getA());
            double[] startPoint = new InteriorPoint(this.random).generate(transformedInequalitiesLhs, constraintsSystem.getB(), new CommonMathGLPSolverWrapper(), startFromRandomizedPoint, true);

            if (consumer == null) {
                samples = this.sampler.sample(transformedInequalitiesLhs, constraintsSystem.getB(), true, startPoint, numberOfSamples);
                samples = transformation.extendBackToOriginalDimensionality(samples);
            } else {
                for (int i = 0; i < numberOfSamples; i++) {
                    double[] sample = this.sampler.sample(transformedInequalitiesLhs, constraintsSystem.getB(), true, startPoint, 1)[0];
                    consumer.consume(transformation.extendBackToOriginalDimensionality(sample));
                    startPoint = sample;
                }
            }
        }

        return samples;
    }
}
