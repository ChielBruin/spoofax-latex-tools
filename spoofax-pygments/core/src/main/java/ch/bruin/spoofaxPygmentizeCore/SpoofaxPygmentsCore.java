package ch.bruin.spoofaxPygmentizeCore;

import org.spoofax.jsglr2.cli.JSGLR2CLI;
import org.spoofax.jsglr2.cli.output.DefaultOutputProcessor;
import picocli.CommandLine;

/**
 * Parser interface for JSGLR2 to produce parse trees in JSON.
 * Used as a backend for the Spoofax-pygments lexers.
 * This is an adaption of the @see <a href="https://github.com/metaborg/jsglr/blob/master/org.spoofax.jsglr2.cli/src/main/java/org/spoofax/jsglr2/cli/JSGLR2CLI.java">JSGLR2CLI</a>.
 */
@CommandLine.Command(name = "Spoofax Pygments Core", sortOptions = false,
        description = "This command-line tool parses pieces of code using the Spoofax JSGLR2 parser and outputs a list of Pygments tokens.%n"
)
public class SpoofaxPygmentsCore extends JSGLR2CLI {

    @CommandLine.Option(names = {"--parseResult"}, description = "Output the default parse result instead of Pygments tokens")
    public void setParseResult(boolean value) {
        outputProcessor = new DefaultOutputProcessor();
    }

    @CommandLine.Option(names = {"--esv"}, description = "The ESV file to use for coloring the Pygments result")
    public static String esvFile;

    public static void main(String[] args) {
        // No extra command-line options needed O:)
        JSGLR2CLI.outputProcessor = new PygmentsTokenOutput();

        int exitCode = new CommandLine(new SpoofaxPygmentsCore()).execute(args);

        System.exit(exitCode);
    }

}
