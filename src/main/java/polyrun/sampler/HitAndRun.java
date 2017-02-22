package polyrun.sampler;

import polyrun.UnitNSphere;
import polyrun.thinning.ThinningFunction;

import java.util.Random;

/**
 * HitAndRun sampler. It provides method to sample uniformly from convex polytope described by a system of linear inequalities Ax &le; b.
 */
public class HitAndRun implements Sampler {
    private final ThinningFunction thinningFunction;
    private final Random random;

    public HitAndRun(ThinningFunction thinningFunction, Random random) {
        this.thinningFunction = thinningFunction;
        this.random = random;
    }

    public HitAndRun(ThinningFunction thinningFunction) {
        this(thinningFunction, new Random());
    }

    @Override
    public double[][] sample(double[][] A, double[] b, boolean homogeneous, double[] startPoint, int numberOfSamples) {
        if (numberOfSamples <= 0) {
            throw new IllegalArgumentException("Argument 'numberOfSamples' has to be greater than 0. Currently, its value equals " + numberOfSamples + ".");
        }

        if (A.length == 0) {
            throw new IllegalArgumentException("Matrix 'A' cannot be empty.");
        }

        int numberOfVariables = A[0].length;
        int numberOfConstraints = A.length;

        if (numberOfVariables == 0) {
            throw new IllegalArgumentException("Matrix 'A' cannot be empty.");
        }

        for (double[] row : A) {
            if (row.length != numberOfVariables) {
                throw new IllegalArgumentException("Length of all rows of matrix 'A' has to be equal.");
            }
        }

        if (A.length != b.length) {
            throw new IllegalArgumentException("Length of vector 'b' has to be equal to the number of rows of matrix 'A' (number of constraints).");
        }

        if (A[0].length != startPoint.length) {
            throw new IllegalArgumentException("Length of vector 'startPoint' has to be equal to the number of columns of matrix 'A' (number of variables).");
        }

        if (homogeneous && startPoint.length < 2) {
            throw new RuntimeException("There is no space to sample.");
        }

        if (homogeneous && Math.abs(startPoint[startPoint.length - 1] - 1.0) > 1e-10) {
            throw new RuntimeException("The last element of 'startPoint' has to be 1.0 in case of providing data in homogeneous coordinates.");
        }

        // check if startPoint is an interior point
        for (int i = 0; i < A.length; i++) {
            double ax = 0.0;

            for (int j = 0; j < A[i].length; j++) {
                ax += A[i][j] * startPoint[j];
            }

            if (ax > b[i]) {
                throw new IllegalArgumentException("Provided 'startPoint' is not an interior point of sampling convex polytope.");
            }
        }

        UnitNSphere unitNSphere = new UnitNSphere(this.random);

        final int thin = Math.max(this.thinningFunction.getThinningFactor(homogeneous ? numberOfVariables - 1 : numberOfVariables), 1);

        double[][] result = new double[numberOfSamples][numberOfVariables];

        double[] ad = new double[numberOfConstraints];
        double[] bax = new double[numberOfConstraints];
        double[] d = new double[numberOfVariables];
        double[] x = startPoint.clone();
        double l;

        for (int sample = 0; sample < numberOfSamples; sample++) {
            for (int q = 0; q < thin; q++) {
                double bg = Double.NaN;
                double ed = Double.NaN;

                unitNSphere.fillVectorWithRandomPoint(d, homogeneous);

                for (int j = 0; j < numberOfConstraints; j++) {
                    ad[j] = 0.0;
                    for (int i = 0; i < numberOfVariables; i++) {
                        ad[j] += A[j][i] * d[i];
                    }
                }

                for (int j = 0; j < numberOfConstraints; j++) {
                    bax[j] = b[j];
                    for (int i = 0; i < numberOfVariables; i++) {
                        bax[j] -= A[j][i] * x[i];
                    }
                }

                for (int j = 0; j < numberOfConstraints; j++) {
                    if (ad[j] < 0.0) {
                        double nV = bax[j] / ad[j];
                        if (Double.isNaN(bg) || bg < nV) {
                            bg = nV;
                        }
                    } else if (ad[j] > 0.0) {
                        double nV = bax[j] / ad[j];
                        if (Double.isNaN(ed) || ed > nV) {
                            ed = nV;
                        }
                    }
                }

                // if space is unbounded ed and/or bg could be NaN, but the probability is very low for little unbounded region

                if (Double.isNaN(bg)) {
                    throw new RuntimeException("Cannot find begin of segment for given direction. The sampling region is unbounded.");
                }

                if (Double.isNaN(ed)) {
                    throw new RuntimeException("Cannot find end of segment for given direction. The sampling region is unbounded.");
                }

                if (bg >= ed) {
                    // bg == ed => polytope defined by provided set of inequalities Ax <= b is not full-dimensional
                    // bg > ed => something went wrong/method error
                    // above cases are considered together due to computer inaccuracy; possible split into two conditions:
                    throw new RuntimeException("Polytope defined by provided set of inequalities is not full-dimensional or method error.");

                    // it also fails if sampler will be right in one of the vertices and direction 'd' will not allow to move anywhere; the probability of such situation goes to zero
                }

                if (bg > 0.0) {
                    // looks like previous point was outside of sampling region or accuracy error
                    throw new RuntimeException("Accuracy or method error (begin of segment).");
                }

                if (ed < 0.0) {
                    // looks like previous point was outside of sampling region or accuracy error
                    throw new RuntimeException("Accuracy or method error (end of segment).");
                }

                l = (bg + (ed - bg) * this.random.nextDouble());

                for (int i = 0; i < numberOfVariables; i++) {
                    x[i] = d[i] * l + x[i];
                }
            }

            System.arraycopy(x, 0, result[sample], 0, numberOfVariables);
        }

        return result;
    }
}
