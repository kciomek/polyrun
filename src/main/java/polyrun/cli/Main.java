package polyrun.cli;

import polyrun.SampleConsumer;
import polyrun.SamplerRunner;
import polyrun.constraints.Constraint;
import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.InputFormatException;
import polyrun.sampler.HitAndRun;
import polyrun.thinning.LinearlyScalableThinningFunction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class Main {
    private static final Set<String> ALLOWED_DIRECTIONS = new HashSet<String>(Arrays.asList("<=", ">=", "="));

    public static void main(String[] args) throws Exception {
        // Parse command line arguments
        final CLI cli = new CLI();
        cli.parse(args);

        // Read constraints
        ConstraintsSystem constraintsSystem = Main.readConstraintSystem(cli.getInputFilePath());

        // Sample
        new SamplerRunner(new HitAndRun(new LinearlyScalableThinningFunction(1.0), new Random(cli.getSeed()))).sample(
                constraintsSystem,
                cli.getNumberOfSamples(),
                new SampleConsumer() {
                    @Override
                    public void consume(double[] sample) {
                        StringBuilder sb = new StringBuilder();

                        for (double val : sample) {
                            sb.append(Double.toString(val)).append("\t");
                        }

                        System.out.println(sb.deleteCharAt(sb.length() - 1).toString());
                    }
                });
    }

    private static ConstraintsSystem readConstraintSystem(String inputFilePath) throws IOException, InputFormatException {
        InputStreamReader reader;

        if (inputFilePath == null) {
            reader = new InputStreamReader(System.in);
        } else {
            reader = new FileReader(inputFilePath);
        }

        BufferedReader br = new BufferedReader(reader);

        Integer requiredLength = null;

        List<Constraint> constraints = new ArrayList<Constraint>();

        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            if (line.length() > 0 && !line.startsWith("#")) {
                String[] fields = line.trim().split("\\s+");

                if (requiredLength == null) {
                    if (fields.length < 3) {
                        throw new InputFormatException(String.format("Wrong input format in line %d. At least three columns required.", lineNumber));
                    }

                    requiredLength = fields.length;
                } else if (fields.length != requiredLength) {
                    throw new InputFormatException(String.format("Wrong input format in line %d. All lines are required to have same number of columns.", lineNumber));
                }

                final double[] lhsVector = new double[requiredLength - 2];
                final String dirElement = fields[requiredLength - 2];
                final double rhsElement = Double.parseDouble(fields[requiredLength - 1]);

                for (int i = 0; i < requiredLength - 2; i++) {
                    lhsVector[i] = Double.parseDouble(fields[i]);
                }

                if (!ALLOWED_DIRECTIONS.contains(dirElement)) {
                    throw new InputFormatException(String.format("Wrong direction symbol in line %d. Expected '>=', '<=' or '=' in, was: '%s'.", lineNumber, dirElement));
                }

                constraints.add(new Constraint() {
                    @Override
                    public double[] getLhs() {
                        return lhsVector;
                    }

                    @Override
                    public String getDirection() {
                        return dirElement;
                    }

                    @Override
                    public double getRhs() {
                        return rhsElement;
                    }
                });
            }
        }

        return new ConstraintsSystem(constraints);
    }
}
