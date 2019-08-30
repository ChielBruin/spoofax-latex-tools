package ch.bruin.spoofaxPygmentizeCore;

import java.io.*;
import java.util.ArrayList;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableReader;
import org.metaborg.parsetable.query.ActionsForCharacterRepresentation;
import org.metaborg.parsetable.query.ProductionToGotoRepresentation;
import org.metaborg.parsetable.states.StateFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.*;
import org.spoofax.jsglr2.imploder.ImploderVariant;
import org.spoofax.jsglr2.parseforest.*;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.tokens.TokenizerVariant;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Parser interface for JSGLR2 to produce parse trees in JSON.
 * Used as a backend for the Spoofax-pygments lexers.
 * This is an adaption of the @see <a href="https://github.com/metaborg/jsglr/blob/master/org.spoofax.jsglr2.cli/src/main/java/org/spoofax/jsglr2/cli/JSGLR2CLI.java">JSGLR2CLI</a>.
 */
@CommandLine.Command(name = "Spoofax Pygments core", sortOptions = false)
public class SpoofaxPygmentsCore implements Runnable {

    @Option(names = { "-pt", "--parseTable" }, required = true,
            description = "Parse table file") private File parseTableFile;

    @Parameters(arity = "1", description = "The input string to be parsed") private String input;

    @Option(names = { "-im", "--implode" }, negatable = true,
            description = "Implode parse tree to AST") private boolean implode = true;

    @ArgGroup(exclusive = false, validate = false, heading = "Parser variant%n") ParserVariantOptions parserVariant =
            new ParserVariantOptions();

    static class ParserVariantOptions {
        @Option(names = { "--activeStacks" },
                description = "Active stacks implementation: ${COMPLETION-CANDIDATES}") private ActiveStacksRepresentation activeStacksRepresentation =
                ActiveStacksRepresentation.standard();

        @Option(names = { "--forActorStacks" },
                description = "For actor stacks implementation: ${COMPLETION-CANDIDATES}") private ForActorStacksRepresentation forActorStacksRepresentation =
                ForActorStacksRepresentation.standard();

        @Option(names = { "--parseForest" },
                description = "Parse forest representation: ${COMPLETION-CANDIDATES}") private ParseForestRepresentation parseForestRepresentation =
                ParseForestRepresentation.standard();

        @Option(names = { "--parseForestConstruction" },
                description = "Parse forest construction method: ${COMPLETION-CANDIDATES}") private ParseForestConstruction parseForestConstruction =
                ParseForestConstruction.standard();

        @Option(names = { "--stack" },
                description = "Stack representation: ${COMPLETION-CANDIDATES}") private StackRepresentation stackRepresentation =
                StackRepresentation.standard();

        @Option(names = { "--reducing" },
                description = "Reducing implementation: ${COMPLETION-CANDIDATES}") private Reducing reducing =
                Reducing.standard();

        @Option(names = { "--imploder" },
                description = "Imploder variant: ${COMPLETION-CANDIDATES}") private ImploderVariant imploderVariant =
                ImploderVariant.standard();

        @Option(names = { "--tokenizer" },
                description = "Tokenizer variant: ${COMPLETION-CANDIDATES}") private TokenizerVariant tokenizerVariant =
                TokenizerVariant.standard();

        JSGLR2Variants.Variant getVariant() throws Exception {
            JSGLR2Variants.ParserVariant parserVariant =
                    new JSGLR2Variants.ParserVariant(activeStacksRepresentation, forActorStacksRepresentation,
                            parseForestRepresentation, parseForestConstruction, stackRepresentation, reducing);

            JSGLR2Variants.Variant variant =
                    new JSGLR2Variants.Variant(parserVariant, imploderVariant, tokenizerVariant);

            if(variant.isValid())
                return variant;
            else
                throw new Exception("Invalid parser variant");
        }
    }

    @ArgGroup(exclusive = false, validate = false,
            heading = "Parse table variant%n") ParseTableVariantOptions parseTableVariant = new ParseTableVariantOptions();

    static class ParseTableVariantOptions {
        @Option(names = { "--actionsForCharacters" },
                description = "Actions for character representation: ${COMPLETION-CANDIDATES}") private ActionsForCharacterRepresentation actionsForCharacterRepresentation =
                ActionsForCharacterRepresentation.standard();

        @Option(names = { "--productionToGoto" },
                description = "Production to goto representation: ${COMPLETION-CANDIDATES}") private ProductionToGotoRepresentation productionToGotoRepresentation =
                ProductionToGotoRepresentation.standard();

    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SpoofaxPygmentsCore()).execute(args);

        System.exit(exitCode);
    }

    public void run() {
        try {
            JSGLR2Variants.Variant variant = parserVariant.getVariant();
            IParseTable parseTable = getParseTable();
            JSGLR2Implementation<?, ?, IStrategoTerm> jsglr2 =
                    (JSGLR2Implementation<?, ?, IStrategoTerm>) JSGLR2Variants.getJSGLR2(parseTable, variant);

            PygmentizeToken root = parse(jsglr2.parser);
            System.out.println(root.toString());
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private PygmentizeToken parse(IParser<?> parser) throws ParseException {
        ParseResult<?> result = parser.parse(input);

        if(result.isSuccess()) {
            ParseSuccess<?> success = (ParseSuccess<?>) result;

            if (success.parseResult instanceof IParseNode) {
                PygmentizeToken rootToken = new PygmentizeToken(0, null, null);
                recurse_tree((IParseNode) success.parseResult, 0, rootToken);
                calculateEndIndices(rootToken);
                return rootToken;
            } else {
                throw new IllegalStateException("The parse result is not an instance of IParseNode, found " + success.getClass().getName());
            }

        } else {
            ParseFailure<?> failure = (ParseFailure<?>) result;
            throw failure.exception();
        }
    }

    private void calculateEndIndices(PygmentizeToken root) {
        // TODO: We need to do a proper tree walk here, but this was quick to hack from the previous version
        ArrayList<PygmentizeToken> tokens = new ArrayList<>();
        root.flatten(tokens);

        if (tokens.size() > 0) {
            for (int i = 0; i < tokens.size() - 1; i++) {
                PygmentizeToken token = tokens.get(i);
                token.setEndIndex(Math.max(token.getStartIndex(), tokens.get(i + 1).getStartIndex()));
            }
            PygmentizeToken token = tokens.get(tokens.size() - 1);
            token.setEndIndex(-1);
        }
    }

    private int recurse_tree(IParseNode node, int index, PygmentizeToken parent) {
        IDerivation derivation = node.getFirstDerivation();

        for (IParseForest pf : derivation.parseForests()) {
            if (pf instanceof IParseNode) {
                IParseNode parseNode = (IParseNode) pf;
                PygmentizeToken token = PygmentizeToken.fromParseNode(parseNode, index);
                if (token != null) {
                    parent.add_child(token);
                }
                index = recurse_tree(parseNode, index, token);
            } else if (pf instanceof ICharacterNode) {
                ICharacterNode charNode = (ICharacterNode) pf;
                index++;
            } else {
                throw new IllegalArgumentException("Error on:" + pf.toString());
            }
        }
        return index;
    }

    private IParseTable getParseTable() throws Exception {
        try (InputStream parseTableInputStream = new FileInputStream(parseTableFile)) {
            ParseTableReader parseTableReader = new ParseTableReader(new StateFactory());
            return parseTableReader.read(parseTableInputStream);
        } catch(IOException e) {
            throw new Exception("Invalid parse table file", e);
        } catch(ParseTableReadException e) {
            throw new Exception("Invalid parse table", e);
        }
    }
}
