package polyrun.cli;

import org.apache.commons.cli.*;
import polyrun.thinning.ConstantThinningFunction;
import polyrun.thinning.LinearlyScalableThinningFunction;
import polyrun.thinning.LogarithmicallyScalableThinningFunction;
import polyrun.thinning.ThinningFunction;

import java.util.Random;

class CLI {
    private static final int DEFAULT_NUMBER_OF_SAMPLES = 1000;
    private static final ThinningFunction DEFAULT_THINNING_FUNCTION = new LinearlyScalableThinningFunction(1.0);

    private final CommandLineParser parser;

    private String inputFilePath;
    private int seed;
    private int numberOfSamples;
    private boolean printStackTraceOnError;
    private ThinningFunction thinningFunction;

    public CLI() {
        this.parser = new DefaultParser();
    }

    private Options generateOptions() {
        Options options = new Options();

        options.addOption(Option.builder("h")
                .required(false)
                .desc("prints help")
                .longOpt("help")
                .build()
        );

        options.addOption(Option.builder("version")
                .required(false)
                .desc("prints version")
                .build()
        );

        options.addOption(Option.builder("s")
                .required(false)
                .hasArg()
                .desc("seed (default: random value)")
                .build()
        );

        options.addOption(Option.builder("i")
                .required(false)
                .hasArg()
                .desc("input file path (if not provided standard input will be used); expected format:\n"
                        + "  1) one constraint per line,\n"
                        + "  2) line format: <a_1> <a_2> ... <a_n> <type> <rhs>, where:\n"
                        + "    <a_1>, <a_2>, ..., <a_n> are coefficients of the variables,\n"
                        + "    <type> denotes a kind of constraint ('<=', '>=' or '='),\n"
                        + "    <rhs> is a constant term,\n"
                        + "    and all fields in line are separated by whitespaces,\n"
                        + "  3) blank lines are skipped,\n"
                        + "  4) optional comment lines are preceded by a hash sign #"
                )
                .build()
        );

        options.addOption(Option.builder("n")
                .required(false)
                .hasArg()
                .desc("number of samples (default: " + DEFAULT_NUMBER_OF_SAMPLES + ")")
                .build()
        );

        options.addOption(Option.builder("t")
                .required(false)
                .desc("thinning function with parameter in format <symbol>:<parameter>, where <symbol> is one of the following:\n"
                        + "  tfc - f(n) = a,\n"
                        + "  tfl - f(n) = ceil(a * n^3),\n"
                        + "  tfg - f(n) = ceil(a * log(n + 1) * n^3),\n"
                        + "and 'a' is <parameter> (default: 'tfl:1')")
                .hasArg()
                .build()
        );

        options.addOption(Option.builder("x")
                .required(false)
                .desc("whether to print stack trace on error or just a message (if not provided: only message)")
                .longOpt("stacktrace")
                .build()
        );

        return options;
    }

    public void parse(String args[]) throws ParseException {
        CommandLine cmd = parser.parse(this.generateOptions(), args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(" ", this.generateOptions());
            System.exit(1);
        }

        if (cmd.hasOption("version")) {
            System.out.println("0.3.0-SNAPSHOT"); // todo: read from resources
            System.exit(0);
        }

        if (cmd.hasOption("i")) {
            this.inputFilePath = cmd.getOptionValue("i");
        } else {
            this.inputFilePath = null;
        }

        if (cmd.hasOption("s")) {
            this.seed = Integer.parseInt(cmd.getOptionValue("s"));
        } else {
            this.seed = new Random().nextInt();
        }

        if (cmd.hasOption("n")) {
            this.numberOfSamples = Integer.parseInt(cmd.getOptionValue("n"));
        } else {
            this.numberOfSamples = DEFAULT_NUMBER_OF_SAMPLES;
        }

        if (cmd.hasOption("t")) {
            String[] fields = cmd.getOptionValue("t").split(":");

            if (fields.length != 2) {
                throw new ParseException("Wrong format of thinning function. Expected <symbol>:<parameter>. See -h for help.");
            }
            if ("tfc".equals(fields[0])) {
                this.thinningFunction = new ConstantThinningFunction(Integer.parseInt(fields[1]));
            } else if ("tfl".equals(fields[0])) {
                this.thinningFunction = new LinearlyScalableThinningFunction(Double.parseDouble(fields[1]));
            } else if ("tfg".equals(fields[0])) {
                this.thinningFunction = new LogarithmicallyScalableThinningFunction(Double.parseDouble(fields[1]));
            } else {
                throw new ParseException("Wrong thinning function symbol '" + fields[0] + "'. See -h for help.");
            }
        } else {
            this.thinningFunction = DEFAULT_THINNING_FUNCTION;
        }

        this.printStackTraceOnError = cmd.hasOption("x");

        if (this.numberOfSamples <= 0) {
            throw new ParseException("Number of samples cannot be less or equal to 0.");
        }
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public int getSeed() {
        return seed;
    }

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public boolean getPrintStackTraceOnError() {
        return printStackTraceOnError;
    }

    public ThinningFunction getThinningFunction() {
        return thinningFunction;
    }
}
