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
 *
 * @deprecated This class will be removed in version 1.0.0. Use {@link PolytopeRunner} instead.
 */
@Deprecated
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
        return this.runSampler(constraintsSystem, numberOfSamples, null, false, null);
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
        return this.runSampler(constraintsSystem, numberOfSamples, null, startFromRandomizedPoint, null);
    }

    /**
     * @param constraintsSystem system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples   number of samples
     * @param consumer          samples consumer
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public void sample(ConstraintsSystem constraintsSystem, int numberOfSamples, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        this.runSampler(constraintsSystem, numberOfSamples, null, false, consumer);
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
        this.runSampler(constraintsSystem, numberOfSamples, null, startFromRandomizedPoint, consumer);
    }

    /**
     * @param constraintsSystem system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples   number of samples
     * @param startPoint        start point, assumed to satisfy constraintsSystem
     * @return
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public double[][] sample(ConstraintsSystem constraintsSystem, int numberOfSamples, double[] startPoint) throws UnboundedSystemException, InfeasibleSystemException {
        return this.runSampler(constraintsSystem, numberOfSamples, startPoint, false, null);
    }

    /**
     * @param constraintsSystem system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples   number of samples
     * @param startPoint        start point, assumed to satisfy constraintsSystem
     * @param consumer          samples consumer
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    public void sample(ConstraintsSystem constraintsSystem, int numberOfSamples, double[] startPoint, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        this.runSampler(constraintsSystem, numberOfSamples, startPoint, false, consumer);
    }

    /**
     * @param constraintsSystem        system of linear constraints (Ax&le;b, Cx=d) that is expected to be consistent and Ax&le;b to be full-dimensional
     * @param numberOfSamples          number of samples
     * @param startPoint               start point, assumed to satisfy constraintsSystem
     * @param startFromRandomizedPoint whether to start from randomized point; taken into account iff start == null
     * @param consumer                 optional samples consumer
     * @return
     * @throws UnboundedSystemException
     * @throws InfeasibleSystemException
     */
    private double[][] runSampler(ConstraintsSystem constraintsSystem, int numberOfSamples, double[] startPoint, boolean startFromRandomizedPoint, SampleConsumer consumer) throws UnboundedSystemException, InfeasibleSystemException {
        if (startPoint != null && constraintsSystem.getNumberOfVariables() != startPoint.length) {
            throw new IllegalArgumentException("Start point has invalid length.");
        }

        Transformation transformation = new Transformation(constraintsSystem.getC(), constraintsSystem.getD(), constraintsSystem.getNumberOfVariables());
        double[][] samples = null;

        if (transformation.getTransformationMatrix()[0].length == 1) {
            if (consumer == null) {
                if (startPoint != null) {
                    throw new RuntimeException("Why to use startPoint when constraint system is reduced to a point?"); //fixme Fix this behaviour
                }

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
            double[] transformedStartPoint;

            if (startPoint == null) {
                transformedStartPoint = new InteriorPoint(this.random).generate(transformedInequalitiesLhs, constraintsSystem.getB(), this.glpSolver, startFromRandomizedPoint, true);
            } else {
                transformedStartPoint = transformation.reduceDimensionality(new double[][]{startPoint})[0];
                transformedStartPoint[transformedStartPoint.length - 1] = 1.0;
            }

            if (consumer == null) {
                samples = this.sampler.sample(transformedInequalitiesLhs, constraintsSystem.getB(), true, transformedStartPoint, numberOfSamples);
                samples = transformation.extendBackToOriginalDimensionality(samples);
            } else {
                for (int i = 0; i < numberOfSamples; i++) {
                    double[] sample = this.sampler.sample(transformedInequalitiesLhs, constraintsSystem.getB(), true, transformedStartPoint, 1)[0];
                    consumer.consume(transformation.extendBackToOriginalDimensionality(sample));
                    transformedStartPoint = sample;
                }
            }
        }

        return samples;
    }
}
