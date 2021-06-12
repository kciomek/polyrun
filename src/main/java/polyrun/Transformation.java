// Copyright (c) 2015-2018 Krzysztof Ciomek
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package polyrun;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

public class Transformation {
    private final SimpleMatrix nullspace;
    private final SimpleMatrix particularSolution;

    /**
     * Builds transformation that is based on system C x = d.
     * <p>
     * Note: Cx = d is expected to be consistent system of linearly independent equations.
     *
     * @param matrixC           matrix C
     * @param vectord           vector d
     * @param numberOfVariables number of variables
     */
    public Transformation(double matrixC[][], double[] vectord, int numberOfVariables) {
        this(matrixC, vectord, numberOfVariables, 1e-10);
    }

    /**
     * Builds transformation that is based on system C x = d.
     * <p>
     * Note: Cx = d is expected to be consistent system of linearly independent equations.
     *
     * @param matrixC           matrix C
     * @param vectord           vector d
     * @param numberOfVariables number of variables
     * @param eps               absolute error to accept in floating point comparisons (non-negative, default 1e-10)
     */
    public Transformation(double matrixC[][], double[] vectord, int numberOfVariables, double eps) {
        if (matrixC == null || matrixC.length == 0) {
            // Set identity matrix as new basis
            this.nullspace = SimpleMatrix.identity(numberOfVariables);

            // Set vector of zeros as particular solution
            this.particularSolution = new SimpleMatrix(numberOfVariables, 1);
        } else {
            Matrix C = new DMatrixRMaj(matrixC);
            SimpleMatrix d = new SimpleMatrix(vectord.length, 1, true, vectord);
            SimpleSVD<SimpleMatrix> svd = new SimpleSVD<SimpleMatrix>(C, false);

            if (svd.nullity() == 0) {
                // There is no space to sample
                throw new RuntimeException("The system of equations has only one solution.");
            }

            // Calculate matrix with inverted singular values on the diagonal
            SimpleMatrix W = svd.getW();
            double[][] values = new double[C.getNumCols()][C.getNumRows()];

            for (int i = 0; i < C.getNumCols(); i++) {
                for (int j = 0; j < C.getNumRows(); j++) {
                    if (i == j) {
                        values[i][j] = W.get(i, i) > eps ? 1.0 / W.get(i, i) : 0.0;
                    } else {
                        values[i][j] = 0.0;
                    }
                }
            }

            // Set null space of Cx = d as new basis
            this.nullspace = svd.nullSpace();

            // Set particular solution, which with the null space describes all solutions of Cx=d
            this.particularSolution = svd.getV().mult(new SimpleMatrix(values)).mult(svd.getU()).mult(d);
        }
    }

    /**
     * Projects input matrix onto the null space to reduce dimensionality.
     * The aim is to prepare the sampling space to be full-dimensional.
     *
     * @param matrixToTransform Matrix to project
     * @return projection
     */
    public double[][] project(double[][] matrixToTransform) {
        if (this.nullspace.numRows() != matrixToTransform[0].length) {
            throw new IllegalArgumentException("Number of columns of matrix 'matrixToTransform' is invalid.");
        }

        SimpleMatrix projection = new SimpleMatrix(matrixToTransform).mult(this.nullspace);
        double[][] result = new double[projection.numRows()][projection.numCols()];

        for (int i = 0; i < projection.numRows(); i++) {
            for (int j = 0; j < projection.numCols(); j++) {
                result[i][j] = projection.get(i, j);
            }
        }

        return result;
    }

    /**
     * Transforms the vector from the sampling space back to the original space.
     *
     * @param vector vector to transform back to original dimensionality
     * @return vector in the original dimensionality
     */
    public double[] projectBack(double[] vector) {
        if (this.nullspace.numCols() != vector.length) {
            throw new IllegalArgumentException("Length of vector is invalid.");
        }

        return new SimpleMatrix(1, vector.length, true, vector)
                .mult(this.nullspace.transpose())
                .plus(this.particularSolution.transpose())
                .getDDRM().getData();
    }

    /**
     * Solves equation b - A * this.particularSolution
     *
     * @param A matrix
     * @param b vector
     * @return result of b - A * this.particularSolution
     */
    public double[] solveForParticularSolution(double[][] A, double[] b) {
        return new SimpleMatrix(b.length, 1, true, b)
                .minus(new SimpleMatrix(A).mult(this.particularSolution))
                .getDDRM().getData();
    }
}
