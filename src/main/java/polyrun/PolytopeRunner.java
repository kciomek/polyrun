package polyrun;

import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InfeasibleSystemException;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.sampling.RandomWalk;
import polyrun.solver.CommonMathGLPSolverWrapper;
import polyrun.solver.GLPSolver;
import polyrun.solver.SolverResult;
import polyrun.thinning.NoThinning;
import polyrun.thinning.ThinningFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents runner that can generates samples from a polytope using any {@link RandomWalk}.
 */
public class PolytopeRunner {

    private final ConstraintsSystem constraintsSystem;
    private final Transformation transformation;
    private double[][] A;
    private final int[][] nonZeroElementsInA;
    private final double[] b;
    private final double[] buffer;

    private double[] startPoint;

    /**
     * @param constraintsSystem system of constraints
     */
    public PolytopeRunner(ConstraintsSystem constraintsSystem) {
        this(constraintsSystem, new CommonMathGLPSolverWrapper(), true);
    }

    /**
     * @param constraintsSystem          system of constraints
     * @param removeRedundantConstraints whether to remove redundant constraints (after initial transformation) or not;
     *                                   use for better performance if there may be some redundant constrains
     * @param skipZeroElements           whether {@code RandomWalk} should iterate over array of indices of non-zero
     *                                   elements or directly over entire transformed matrix A (from {@code constraintsSystem})
     */
    public PolytopeRunner(ConstraintsSystem constraintsSystem, boolean removeRedundantConstraints, boolean skipZeroElements) {
        this(constraintsSystem, removeRedundantConstraints ? new CommonMathGLPSolverWrapper() : null, skipZeroElements);
    }

    /**
     * @param constraintsSystem system of constraints
     * @param glpSolver         solver; if not null redundant constrains will be removed;
     *                          provide for better performance if there may be some redundant constrains
     * @param skipZeroElements  whether {@code RandomWalk} should iterate over array of indices of non-zero
     *                          elements or directly over entire transformed matrix A (from {@code constraintsSystem})
     */
    public PolytopeRunner(ConstraintsSystem constraintsSystem, GLPSolver glpSolver, boolean skipZeroElements) {
        this.constraintsSystem = constraintsSystem;
        this.transformation = new Transformation(constraintsSystem.getC(), constraintsSystem.getD(), constraintsSystem.getNumberOfVariables());

        double[][] transformedA = transformation.project(constraintsSystem.getA());
        double[] transformedB = transformation.solveForParticularSolution(constraintsSystem.getA(), constraintsSystem.getB());

        if (glpSolver != null) {
            List<Integer> redundantConstraints = new ArrayList<Integer>();

            for (int i = 0; i < transformedA.length; i++) {
                double[] reducedB = new double[transformedA.length];
                System.arraycopy(transformedB, 0, reducedB, 0, transformedB.length);
                reducedB[i] += 1;

                try {
                    SolverResult solverResult = glpSolver.solve(GLPSolver.Direction.Maximize,
                            transformedA[i],
                            new ConstraintsSystem(transformedA, reducedB));

                    if (!solverResult.isFeasible()) {
                        throw new RuntimeException("Infeasible system.");
                    }

                    if (solverResult.getValue() < transformedB[i]) {
                        redundantConstraints.add(i);
                    }
                } catch (UnboundedSystemException e) {
                    throw new RuntimeException("Unbounded system.", e);
                }
            }

            this.A = new double[transformedA.length - redundantConstraints.size()][];
            this.b = new double[transformedA.length - redundantConstraints.size()];

            int index = 0;
            for (int i = 0; i < transformedA.length; i++) {
                if (!redundantConstraints.contains(i)) {
                    this.A[index] = transformedA[i];
                    this.b[index] = transformedB[i];
                    index++;
                }
            }
        } else {
            this.A = transformedA;
            this.b = transformedB;
        }

        if (skipZeroElements) {
            this.nonZeroElementsInA = new int[A.length][];

            for (int i = 0; i < this.A.length; i++) {
                List<Integer> row = new ArrayList<Integer>();

                for (int j = 0; j < this.A[i].length; j++) {
                    if (this.A[i][j] != 0.0) {
                        row.add(j);
                    }
                }

                int[] iRow = new int[row.size()];
                for (int j = 0; j < row.size(); j++) {
                    iRow[j] = row.get(j);
                }

                nonZeroElementsInA[i] = iRow;
            }
        } else {
            this.nonZeroElementsInA = null;
        }

        this.buffer = new double[this.A[0].length];
    }

    private void next(RandomWalk randomWalk, ThinningFunction thinningFunction, int numberOfSamples, SampleConsumer consumer, double[] dest) {
        if (this.startPoint == null) {
            throw new RuntimeException("Start point is not set. Use method setStartPoint() or setAnyStartPoint().");
        }

        final int stepsPerSample = thinningFunction.getThinningFactor(this.A.length, this.A[0].length);

        for (int i = 0; i < numberOfSamples; i++) {
            for (int j = 0; j < stepsPerSample; j++) {
                randomWalk.next(A, nonZeroElementsInA, b, buffer, startPoint, dest);
            }

            consumer.consume(this.transformation.projectBack(dest));
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
        this.startPoint = new double[A[0].length];
        // values has to be copied because this.startPoint can be changed by method chain
        System.arraycopy(new InteriorPoint().generate(this.A, this.b, glpSolver), 0, startPoint, 0, A[0].length);
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

        double[] transformedPoint = this.transformation.project(new double[][]{startPoint})[0];

        if (!ConstraintsSystem.isSatisfied(A, transformedPoint, b)) {
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

        return this.transformation.projectBack(this.startPoint);
    }
}
