package polyrun;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

class MatrixUtils {
    public static boolean isIdentity(Matrix matrix, double accuracy) {
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                if (i == j) {
                    if (Math.abs(matrix.get(i, j) - 1.0) > accuracy) {
                        return false;
                    }
                } else {
                    if (Math.abs(matrix.get(i, j)) > accuracy) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static Matrix pseudoInverse(Matrix matrix, double accuracy) {
        SingularValueDecomposition svd;
        Matrix U_transposed;
        Matrix V;

        if (matrix.getColumnDimension() > matrix.getRowDimension()) {
            svd = matrix.transpose().svd();
            V = svd.getU();
            U_transposed = svd.getV().transpose();
        } else {
            svd = matrix.svd();
            U_transposed = svd.getU().transpose();
            V = svd.getV();
        }

        double[][] d = new double[svd.getSingularValues().length][1];

        for (int i = 0; i < svd.getSingularValues().length; i++) {
            if (Math.abs(svd.getSingularValues()[i]) <= accuracy) {
                d[i][0] = 0.0;
            } else {
                d[i][0] = 1.0 / svd.getSingularValues()[i];
            }
        }

        for (int i = 0; i < U_transposed.getRowDimension(); i++) {
            for (int j = 0; j < U_transposed.getColumnDimension(); j++) {
                U_transposed.set(i, j, U_transposed.get(i, j) * d[i][0]);
            }
        }

        return V.times(U_transposed);
    }
}
