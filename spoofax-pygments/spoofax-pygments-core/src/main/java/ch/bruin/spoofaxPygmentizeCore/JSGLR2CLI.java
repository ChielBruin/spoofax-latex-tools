package ch.bruin.spoofaxPygmentizeCore;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableReader;
import org.metaborg.parsetable.query.ActionsForCharacterRepresentation;
import org.metaborg.parsetable.query.ProductionToGotoRepresentation;
import org.metaborg.parsetable.states.IStateFactory;
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

@CommandLine.Command(name = "JSGLR2 CLI", sortOptions = false)
public class JSGLR2CLI implements Runnable {

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
        int exitCode = new CommandLine(new JSGLR2CLI()).execute(args);

        System.exit(exitCode);
    }

    private OutputStream outputStream;

    public void run() {
        try {
            JSGLR2Variants.Variant variant = parserVariant.getVariant();
            IParseTable parseTable = getParseTable();
            JSGLR2Implementation<?, ?, IStrategoTerm> jsglr2 =
                    (JSGLR2Implementation<?, ?, IStrategoTerm>) JSGLR2Variants.getJSGLR2(parseTable, variant);

            List<PygmentizeToken> tokens = parse(jsglr2.parser);
            System.out.println(tokens.toString());
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private List<PygmentizeToken> parse(IParser<?> parser) throws ParseException {
//        StringBuilder input_content = new StringBuilder();
//        try (BufferedReader br = new BufferedReader(new FileReader(new File(input)))) {
//            while (true) {
//                String line = br.readLine();
//                if (line == null) break;
//                input_content.append(line);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ParseResult<?> result = parser.parse(input);

        if(result.isSuccess()) {
            ParseSuccess<?> success = (ParseSuccess<?>) result;

            if (success.parseResult instanceof  IParseNode) {
                List<PygmentizeToken> tokens = new ArrayList<>();
                recurse_tree((IParseNode) success.parseResult, tokens, 0);
                return calculateEndIndices(tokens);
            } else {
                throw new IllegalStateException("The parse result is not an instance of IParseNode, found " + success.getClass().getName());
            }

        } else {
            ParseFailure<?> failure = (ParseFailure<?>) result;
            throw failure.exception();
        }
    }

    private List<PygmentizeToken> calculateEndIndices(List<PygmentizeToken> tokens) {
        if (tokens.size() > 0) {
            for (int i = 0; i < tokens.size() - 1; i++) {
                PygmentizeToken token = tokens.get(i);
                token.setEndIndex(Math.max(token.getStartIndex(), tokens.get(i + 1).getStartIndex()));
            }
            PygmentizeToken token = tokens.get(tokens.size() - 1);
            token.setEndIndex(-1);
        }
        return tokens;
    }

    private int recurse_tree(IParseNode node, List<PygmentizeToken> tokens, int index) {
        IDerivation derivation = node.getFirstDerivation();

        for (IParseForest pf : derivation.parseForests()) {
            if (pf instanceof IParseNode) {
                IParseNode parseNode = (IParseNode) pf;
                PygmentizeToken token = PygmentizeToken.fromParseNode(parseNode, index);
                if (token != null) {
                    tokens.add(token);
                }
                index = recurse_tree(parseNode, tokens, index);
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
        try {
            InputStream parseTableInputStream = new FileInputStream(parseTableFile);
            IStateFactory stateFactory =
                    new StateFactory(ActionsForCharacterRepresentation.standard(), ProductionToGotoRepresentation.standard());

            ParseTableReader parseTableReader = new ParseTableReader(new StateFactory());

            return parseTableReader.read(parseTableInputStream);
        } catch(IOException e) {
            throw new Exception("Invalid parse table file", e);
        } catch(ParseTableReadException e) {
            throw new Exception("Invalid parse table", e);
        }
    }
}
