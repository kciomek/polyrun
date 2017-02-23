package polyrun;

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.sampler.Sampler;
import polyrun.solver.CommonMathGLPSolverWrapper;
import polyrun.solver.GLPSolver;

import java.util.Random;

/**
 * Runner of {@link Sampler}. Prepares system of constraints, generates start point and allows to process samples
 * of generated sampled using interface {@link SampleConsumer}.
 */
public class SamplerRunner {
    private final Sampler sampler;
    private final Random random;
    private final GLPSolver glpSolver;

    public SamplerRunner(Sampler sampler, Random random, GLPSolver glpSolver) {
        this.sampler = sampler;
        this.random = random;
        this.glpSolver = glpSolver;
    }

    public SamplerRunner(Sampler sampler, Random random) {
        this(sampler, random, new CommonMathGLPSolverWrapper());
    }

    public SamplerRunner(Sampler sampler, GLPSolver glpSolver) {
        this(sampler, new Random(), glpSolver);
    }

    public SamplerRunner(Sampler sampler) {
        this(sampler, new Random(), new CommonMathGLPSolverWrapper());
    }

    /**
     * @param constraintsSystem system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples   number of samples
     * @return samples
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public double[][] sample(ConstraintsSystem constraintsSystem, int numberOfSamples) throws UnboundedSystemException, InfeasibleSystemException {
        return this.runSampler(constraintsSystem, numberOfSamples, false, null);
    }

    /**
     * @param constraintsSystem        system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples          number of samples
     * @param startFromRandomizedPoint whether to start from randomized point
     * @return samples
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public double[][] sample(ConstraintsSystem constraintsSystem, int numberOfSamples, boolean startFromRandomizedPoint) throws UnboundedSystemException, InfeasibleSystemException {
        return this.runSampler(constraintsSystem, numberOfSamples, startFromRandomizedPoint, null);
    }

    /**
     * @param constraintsSystem system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples   number of samples
     * @param consumer          samples consumer
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public void sample(ConstraintsSystem constraintsSystem, int numberOfSamples, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        this.runSampler(constraintsSystem, numberOfSamples, false, consumer);
    }

    /**
     * @param constraintsSystem        system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples          number of samples
     * @param startFromRandomizedPoint whether to start from randomized point
     * @param consumer                 samples consumer
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
            double[] startPoint = new InteriorPoint(this.random).generate(transformedInequalitiesLhs, constraintsSystem.getB(), this.glpSolver, startFromRandomizedPoint, true);

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
