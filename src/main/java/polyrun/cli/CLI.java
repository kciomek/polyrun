package polyrun.cli;

import org.apache.commons.cli.*;

import java.util.Random;

class CLI {
    private static final int DEFAULT_NUMBER_OF_SAMPLES = 1000;

    private final CommandLineParser parser;

    private String inputFilePath;
    private int seed;
    private int numberOfSamples;

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

        options.addOption(Option.builder("s")
                .required(false)
                .hasArg()
                .desc("seed (default: random value)")
                .build()
        );

        options.addOption(Option.builder("i")
                .required(false)
                .hasArg()
                .desc("input file path (if not provided standard input will be used)")
                .build()
        );

        options.addOption(Option.builder("n")
                .required(false)
                .hasArg()
                .desc("number of samples (default: " + DEFAULT_NUMBER_OF_SAMPLES + ")")
                .build()
        );

        return options;
    }

    public void parse(String args[]) throws ParseException {
        CommandLine cmd = parser.parse(this.generateOptions(), args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("...", this.generateOptions());
            System.exit(1);
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
}
