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
        if (matrixC == null || matrixC.length == 0) {
            this.nullspace = SimpleMatrix.identity(numberOfVariables);
            this.particularSolution = new SimpleMatrix(numberOfVariables, 1);
        } else {
            Matrix C = new DMatrixRMaj(matrixC);
            SimpleMatrix d = new SimpleMatrix(vectord.length, 1, true, vectord);
            SimpleSVD<SimpleMatrix> svd = new SimpleSVD<SimpleMatrix>(C, false);

            if (svd.nullity() == 0) {
                throw new RuntimeException("The system of equations has only one solution.");
            }

            SimpleMatrix W = svd.getW();

            double[][] values = new double[C.getNumCols()][C.getNumRows()];

            for (int i = 0; i < C.getNumCols(); i++) {
                for (int j = 0; j < C.getNumRows(); j++) {
                    if (i == j) {
                        values[i][j] = W.get(i, i) > 1e-10 ? 1.0 / W.get(i, i) : 0.0;
                    } else {
                        values[i][j] = 0.0;
                    }
                }
            }

            this.nullspace = svd.nullSpace();
            this.particularSolution = svd.getV().mult(new SimpleMatrix(values)).mult(svd.getU()).mult(d);
        }
    }

    /**
     * Projects input matrix onto the null space to reduce dimensionality.
     * The aim is to prepare the sampling space to be full-dimensional.
     *
     * @param matrixToTransform Matrix to project
     * @return
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
     * @param A
     * @param b
     * @return
     */
    public double[] solveForParticularSolution(double[][] A, double[] b) {
        return new SimpleMatrix(b.length, 1, true, b)
                .minus(new SimpleMatrix(A).mult(this.particularSolution))
                .getDDRM().getData();
    }
}
