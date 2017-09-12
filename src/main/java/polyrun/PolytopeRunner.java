package polyrun;

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.sampling.RandomWalk;
import polyrun.solver.CommonMathGLPSolverWrapper;
import polyrun.solver.GLPSolver;
import polyrun.thinning.NoThinning;
import polyrun.thinning.ThinningFunction;

/**
 * Represents runner that can generates samples from a polytope using any {@link RandomWalk}.
 */
public class PolytopeRunner {

    private final ConstraintsSystem constraintsSystem;
    private final Transformation transformation;
    private final double[][] transformedA;

    private final double[] adBuffer;
    private final double[] baxBuffer;
    private final double[] directionBuffer;

    private double[] startPoint;

    public PolytopeRunner(ConstraintsSystem constraintsSystem) {
        this.constraintsSystem = constraintsSystem;
        this.transformation = new Transformation(constraintsSystem.getC(), constraintsSystem.getD(), constraintsSystem.getNumberOfVariables());

        if (transformation.getTransformationMatrix()[0].length == 1) {
            throw new IllegalArgumentException("Polytope is reduced to a point.");
        } else {
            this.transformedA = transformation.reduceDimensionality(constraintsSystem.getA());
            this.adBuffer = new double[this.constraintsSystem.getB().length];
            this.baxBuffer = new double[this.constraintsSystem.getB().length];
            this.directionBuffer = new double[this.transformedA[0].length];
        }
    }

    private void next(RandomWalk randomWalk, ThinningFunction thinningFunction, int numberOfSamples, SampleConsumer consumer, double[] dest) {
        if (this.startPoint == null) {
            throw new RuntimeException("Start point is not set. Use method setStartPoint() or setAnyStartPoint().");
        }

        final int toSkip = thinningFunction.getThinningFactor(transformedA[0].length - 1);

        for (int i = 0; i < numberOfSamples; i++) {
            for (int j = 0; j < toSkip; j++) {
                randomWalk.next(transformedA, constraintsSystem.getB(), true,
                        adBuffer, baxBuffer, directionBuffer,
                        startPoint, dest);
            }

            consumer.consume(this.transformation.extendBackToOriginalDimensionality(dest));
        }
    }

    /**
     * Generates a chain of samples starting from {@link PolytopeRunner#startPoint}. The method uses
     * {@link PolytopeRunner#startPoint} for storing next steps and finally {@link PolytopeRunner#startPoint} is set
     * to the last sample from the chain - to be a start point in next call of {@code chain} or {@code neighborhood}.
     *
     * @param randomWalk       sampler
     * @param thinningFunction thinningFunction (see {@link polyrun.thinning})
     * @param numberOfSamples  number of samples
     * @param consumer         samples consumer
     */
    public void chain(RandomWalk randomWalk, ThinningFunction thinningFunction, int numberOfSamples, SampleConsumer consumer) {
        this.next(randomWalk, thinningFunction, numberOfSamples, consumer, startPoint);
    }

    /**
     * Generates samples from the neighborhood of {@link PolytopeRunner#startPoint}. The method does not change
     * {@link PolytopeRunner#startPoint}.
     *
     * @param randomWalk      sampler
     * @param numberOfSamples number of samples
     * @param consumer        samples consumer
     */
    public void neighborhood(RandomWalk randomWalk, int numberOfSamples, SampleConsumer consumer) {
        double[] neighbour = new double[startPoint.length];
        this.next(randomWalk, new NoThinning(), numberOfSamples, consumer, neighbour);
    }

    /**
     * Generates a chain of samples starting from {@link PolytopeRunner#startPoint}. The method uses
     * {@link PolytopeRunner#startPoint} for storing next steps and finally it is set to the last sample from the chain
     * to be a start point in next call of {@code chain} or {@code neighborhood}.
     *
     * @param randomWalk       sampler
     * @param thinningFunction thinning function (see {@link polyrun.thinning})
     * @param numberOfSamples  number of samples
     * @return array of generated samples
     */
    public double[][] chain(RandomWalk randomWalk, ThinningFunction thinningFunction, int numberOfSamples) {
        final double[][] result = new double[numberOfSamples][];
        final int[] index = {0};

        this.chain(randomWalk, thinningFunction, numberOfSamples, new SampleConsumer() {
            @Override
            public void consume(double[] sample) {
                result[index[0]++] = sample;
            }
        });

        return result;
    }

    /**
     * Generates samples from the neighborhood of {@link PolytopeRunner#startPoint}. The method does not change
     * {@link PolytopeRunner#startPoint}.
     *
     * @param randomWalk      sampler
     * @param numberOfSamples number of samples
     * @return array of generated samples
     */
    public double[][] neighborhood(RandomWalk randomWalk, int numberOfSamples) {
        final double[][] result = new double[numberOfSamples][];
        final int[] index = {0};

        this.neighborhood(randomWalk, numberOfSamples, new SampleConsumer() {
            @Override
            public void consume(double[] sample) {
                result[index[0]++] = sample;
            }
        });

        return result;
    }

    /**
     * Sets start point for methods {@code chain} and {@code neighborhood}. Set point is calculated by slack
     * maximization between edges.
     *
     * @throws UnboundedSystemException  if a polytope is tried to be built on top of unbounded system of constraints
     *                                   (in such a case polytope cannot be sampled)
     * @throws InfeasibleSystemException if a polytope is tried to be built on top of infeasible system of constraints
     *                                   (in such a case polytope cannot be sampled)
     */
    public void setAnyStartPoint() throws UnboundedSystemException, InfeasibleSystemException {
        this.setAnyStartPoint(new CommonMathGLPSolverWrapper());
    }

    /**
     * Sets start point for methods {@code chain} and {@code neighborhood}. Set point is calculated by slack
     * maximization between edges.
     *
     * @param glpSolver solver for General Linear Programing problem (e.g., {@link CommonMathGLPSolverWrapper})
     * @throws UnboundedSystemException  if a polytope is tried to be build on top of unbounded system of constraints
     *                                   (in such a case polytope cannot be sampled)
     * @throws InfeasibleSystemException if a polytope is tried to be build on top of infeasible system of constraints
     *                                   (in such a case polytope cannot be sampled)
     */
    public void setAnyStartPoint(GLPSolver glpSolver) throws UnboundedSystemException, InfeasibleSystemException {
        this.startPoint = new InteriorPoint().generate(this.transformedA, constraintsSystem.getB(), glpSolver, false, true);
    }

    /**
     * Sets start point for methods {@code chain} and {@code neighborhood}.
     *
     * @param startPoint point to be set
     * @throws IllegalArgumentException if a startPoint is not an interior point of the polytope
     */
    public void setStartPoint(double[] startPoint) {
        if (startPoint.length != this.constraintsSystem.getA()[0].length) {
            throw new IllegalArgumentException("Length of start point has to be equal to the number of columns in constraints system.");
        }

        double[] transformedPoint = this.transformation.reduceDimensionality(new double[][]{startPoint})[0];
        transformedPoint[transformedPoint.length - 1] = 1.0;

        if (!isInterior(transformedPoint)) {
            throw new IllegalArgumentException("Interior point is required.");
        }

        this.startPoint = transformedPoint;
    }

    /**
     * Gets start point used as an initial point in methods {@code chain} and {@code neighborhood}.
     *
     * @return start point
     */
    public double[] getStartPoint() {
        if (this.startPoint == null) {
            return null;
        }

        return this.transformation.extendBackToOriginalDimensionality(this.startPoint);
    }

    private boolean isInterior(double[] pointToCheck) {
        for (int i = 0; i < this.transformedA.length; i++) {
            double ax = 0.0;

            for (int j = 0; j < this.transformedA[i].length; j++) {
                ax += this.transformedA[i][j] * pointToCheck[j];
            }

            if (ax > this.constraintsSystem.getB()[i]) {
                return false;
            }
        }

        return true;
    }
}
