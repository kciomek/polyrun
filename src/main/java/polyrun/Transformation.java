package polyrun;

import Jama.Matrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RRQRDecomposition;

public class Transformation {
    private final Matrix transformationMatrix;
    private final double[] translationVector;

    /**
     * Builds transformation that is based on system C x = d.
     * <p>
     * Note: Cx = d is expected to be consistent system of equations.
     *
     * @param matrixC           matrix C
     * @param vectord           vector d
     * @param numberOfVariables number of variables
     */
    public Transformation(double matrixC[][], double[] vectord, int numberOfVariables) {
        Matrix basis;
        Matrix translation;

        if (matrixC.length == 0) {
            basis = Matrix.identity(numberOfVariables + 1, numberOfVariables + 1);
            translation = new Matrix(numberOfVariables, 1, 0);
        } else {
            Matrix C = new Matrix(matrixC);
            Matrix C_pseudoinverted = MatrixUtils.pseudoInverse(C, 1e-16);
            Matrix C_pseudoinverted_multiplied_by_C = C_pseudoinverted.times(C);
            Matrix I = Matrix.identity(C_pseudoinverted_multiplied_by_C.getRowDimension(),
                    C_pseudoinverted_multiplied_by_C.getColumnDimension());

            if (MatrixUtils.isIdentity(C_pseudoinverted_multiplied_by_C, 1e-16)) {
                basis = new Matrix(C_pseudoinverted_multiplied_by_C.getColumnDimension() + 1, 1, 0);
                basis.set(C_pseudoinverted_multiplied_by_C.getColumnDimension(), 0, 1);
            } else {
                I.minusEquals(C_pseudoinverted_multiplied_by_C);

                RRQRDecomposition rrqr = new org.apache.commons.math3.linear.RRQRDecomposition(new Array2DRowRealMatrix(I.getArrayCopy()));
                Matrix Q = new Matrix(rrqr.getQ().getData());
                Matrix Q1 = Q.getMatrix(0, C_pseudoinverted_multiplied_by_C.getRowDimension() - 1, 0, I.rank() - 1);

                basis = new Matrix(Q1.getRowDimension() + 1, Q1.getColumnDimension() + 1);
                basis.set(Q1.getRowDimension(), Q1.getColumnDimension(), 1);
                basis.setMatrix(0, Q1.getRowDimension() - 1, 0, Q1.getColumnDimension() - 1, Q1);
            }

            translation = C_pseudoinverted.times(new Matrix(new double[][]{vectord}).transpose());
        }

        Matrix T = Matrix.identity(translation.getRowDimension() + 1, translation.getRowDimension() + 1);
        T.setMatrix(0, translation.getRowDimension() - 1, translation.getRowDimension(), translation.getRowDimension(), translation);
        Matrix lastRowRemover = Matrix.identity(translation.getRowDimension(), translation.getRowDimension() + 1);

        this.transformationMatrix = lastRowRemover.times(T).times(basis);
        this.translationVector = translation.transpose().getArray()[0];
    }

    public double[][] getTransformationMatrix() {
        return this.transformationMatrix.getArray();
    }

    public double[] getTranslationVector() {
        return this.translationVector;
    }

    public double[][] reduceDimensionality(double[][] matrixToTransform) {
        if (this.transformationMatrix.getRowDimension() != matrixToTransform[0].length) {
            throw new IllegalArgumentException("Number of columns of matrix 'matrixToTransform' is invalid.");
        }

        return new Matrix(matrixToTransform).times(this.transformationMatrix).getArray();
    }

    /**
     * Returns result of the following Matrix multiplication: [vector 1.0] * t(transformationMatrix), where
     * vector is a parameter, and transformationMatrix is an internal matrix representation of the transformation.
     *
     * @param vector vector to transform back to original dimension
     * @return vector in original dimension
     */
    public double[] extendBackToOriginalDimensionality(double[] vector) {
        if (this.transformationMatrix.getColumnDimension() != vector.length) {
            throw new IllegalArgumentException("Length of vector is invalid.");
        }

        double[] vectorInOriginalDimensionality = new double[this.transformationMatrix.getRowDimension()];

        for (int i = 0; i < this.transformationMatrix.getRowDimension(); i++) {
            vectorInOriginalDimensionality[i] = 0.0;

            for (int j = 0; j < this.transformationMatrix.getColumnDimension(); j++) {
                double vectorCell;
                if (j < vector.length) {
                    vectorCell = vector[j];
                } else {
                    vectorCell = 1.0;
                }

                vectorInOriginalDimensionality[i] += vectorCell * this.transformationMatrix.get(i, j);
            }
        }

        return vectorInOriginalDimensionality;
    }

    public double[][] extendBackToOriginalDimensionality(double[][] matrix) {
        double[][] result = new double[matrix.length][this.transformationMatrix.getRowDimension()];

        for (int i = 0; i < result.length; i++) {
            result[i] = this.extendBackToOriginalDimensionality(matrix[i]);
        }

        return result;
    }
}
