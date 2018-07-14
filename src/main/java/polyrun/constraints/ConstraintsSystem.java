package polyrun.constraints;

import java.util.Collection;

/**
 * System of linear constraints represented as matrices A, C and vectors b, d, where
 * Ax &le; b,
 * Cx = d.
 */
public class ConstraintsSystem {
    private final double[][] A;
    private final double[] b;
    private final double[][] C;
    private final double[] d;
    private final int numberOfVariables;

    public ConstraintsSystem(Collection<Constraint> constraints) {
        if (constraints.size() == 0) {
            throw new IllegalArgumentException("Constraints cannot be empty.");
        }

        Integer numberOfVariables = null;

        int numberOfEqualities = 0;

        for (Constraint constraint : constraints) {
            if (constraint.getDirection().equals("=")) {
                numberOfEqualities++;
            }
        }

        this.A = new double[constraints.size() - numberOfEqualities][];
        this.b = new double[constraints.size() - numberOfEqualities];
        this.C = new double[numberOfEqualities][];
        this.d = new double[numberOfEqualities];

        int inequalityIndex = 0;
        int equalityIndex = 0;

        for (Constraint constraint : constraints) {
            String dir = constraint.getDirection();
            double[] lhs = constraint.getLhs();
            double rhs = constraint.getRhs();

            if (numberOfVariables == null) {
                numberOfVariables = lhs.length;
            } else if (lhs.length != numberOfVariables) {
                throw new IllegalArgumentException("All rows of A are expected to be of equal length."); // just to be sure
            }

            double[] a = new double[numberOfVariables];
            double b;

            if (dir.equals("=") || dir.equals("<=")) {
                System.arraycopy(lhs, 0, a, 0, numberOfVariables);
                b = rhs;
            } else if (dir.equals(">=")) {
                for (int j = 0; j < numberOfVariables; j++) {
                    a[j] = -lhs[j];
                }
                b = -rhs;
            } else {
                throw new IllegalArgumentException("Wrong symbol of direction. Only '<=', '=>' and '=' are acceptable.");
            }

            if (dir.equals("=")) {
                this.C[equalityIndex] = a;
                this.d[equalityIndex] = b;
                equalityIndex++;
            } else {
                this.A[inequalityIndex] = a;
                this.b[inequalityIndex] = b;
                inequalityIndex++;
            }
        }

        this.numberOfVariables = numberOfVariables;
    }

    public ConstraintsSystem(double[][] lhs, String[] dir, double[] rhs) {
        if (lhs.length == 0 || lhs[0].length == 0) {
            throw new IllegalArgumentException("Matrix 'lhs' cannot have 0 rows and/or 0 columns.");
        }

        if (lhs.length != dir.length) {
            throw new IllegalArgumentException("Length of vector 'dir' has to be equal to the number of rows of matrix 'lhs'.");
        }

        if (lhs.length != rhs.length) {
            throw new IllegalArgumentException("Length of vector 'rhs' has to be equal to the number of rows of matrix 'lhs'.");
        }

        this.numberOfVariables = lhs[0].length;

        for (int i = 1; i < lhs.length; i++) { // just to be sure
            if (lhs[i].length != this.numberOfVariables) {
                throw new IllegalArgumentException("All rows of A are expected to be of equal length.");
            }
        }

        int numberOfEqualities = 0;

        for (String constraintDirection : dir) {
            if (constraintDirection.equals("=")) {
                numberOfEqualities++;
            }
        }

        this.A = new double[lhs.length - numberOfEqualities][];
        this.b = new double[lhs.length - numberOfEqualities];
        this.C = new double[numberOfEqualities][];
        this.d = new double[numberOfEqualities];

        int inequalityIndex = 0;
        int equalityIndex = 0;

        for (int i = 0; i < lhs.length; i++) {
            double[] a = new double[this.numberOfVariables];
            double b;

            if (dir[i].equals("=") || dir[i].equals("<=")) {
                System.arraycopy(lhs[i], 0, a, 0, this.numberOfVariables);
                b = rhs[i];
            } else if (dir[i].equals(">=")) {
                for (int j = 0; j < this.numberOfVariables; j++) {
                    a[j] = -lhs[i][j];
                }
                b = -rhs[i];
            } else {
                throw new IllegalArgumentException("Wrong symbol of direction. Only '<=', '=>' and '=' are acceptable.");
            }

            if (dir[i].equals("=")) {
                this.C[equalityIndex] = a;
                this.d[equalityIndex] = b;
                equalityIndex++;
            } else {
                this.A[inequalityIndex] = a;
                this.b[inequalityIndex] = b;
                inequalityIndex++;
            }
        }
    }

    public ConstraintsSystem(double[][] A, double[] b, double[][] C, double[] d) {

        if (A.length > 0) {
            this.numberOfVariables = A[0].length;
        } else if (C.length > 0) {
            this.numberOfVariables = C[0].length;
        } else {
            throw new IllegalArgumentException("Matrix A and C are empty.");
        }

        if (A.length != b.length) {
            throw new IllegalArgumentException("Length of vector 'b' has to be equal to the number of rows of matrix 'A'.");
        }

        if (C.length != d.length) {
            throw new IllegalArgumentException("Length of vector 'd' has to be equal to the number of rows of matrix 'C'.");
        }

        for (double[] row : A) { // just to be sure
            if (row.length != this.numberOfVariables) {
                throw new IllegalArgumentException("All rows of A and C are expected to be of equal length.");
            }
        }

        for (double[] row : C) { // just to be sure
            if (row.length != this.numberOfVariables) {
                throw new IllegalArgumentException("All rows of A and C are expected to be of equal length.");
            }
        }

        this.A = A;
        this.b = b;
        this.C = C;
        this.d = d;
    }

    public ConstraintsSystem(double[][] A, double[] b) {
        this(A, b, new double[0][], new double[0]);
    }

    public double[][] getA() {
        return this.A;
    }

    public double[] getB() {
        return this.b;
    }

    public double[][] getC() {
        return this.C;
    }

    public double[] getD() {
        return this.d;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    /**
     * Checks if x satisfy system of inequalities: Ax &le; b.
     *
     * @param A matrix
     * @param x vector
     * @param b vector
     * @return whether Ax &le; b is satisfied
     */
    public static boolean isSatisfied(double[][] A, double[] x, double[] b) {
        for (int i = 0; i < A.length; i++) {
            double ax = 0.0;

            for (int j = 0; j < A[i].length; j++) {
                ax += A[i][j] * x[j];
            }

            if (ax > b[i]) {
                return false;
            }
        }

        return true;
    }
}
