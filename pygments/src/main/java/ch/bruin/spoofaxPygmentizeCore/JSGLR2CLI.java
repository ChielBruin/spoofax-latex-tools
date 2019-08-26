package ch.bruin.spoofaxPygmentizeCore;

import java.io.*;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableReader;
import org.metaborg.parsetable.query.ActionsForCharacterRepresentation;
import org.metaborg.parsetable.query.ProductionToGotoRepresentation;
import org.metaborg.parsetable.states.IStateFactory;
import org.metaborg.parsetable.states.StateFactory;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr2.*;
import org.spoofax.jsglr2.imploder.ImploderVariant;
import org.spoofax.jsglr2.parseforest.*;
import org.spoofax.jsglr2.parseforest.hybrid.HybridDerivation;
import org.spoofax.jsglr2.parseforest.hybrid.HybridParseNode;
import org.spoofax.jsglr2.parser.IObservableParser;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseResult;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.tokens.TokenizerVariant;

import org.spoofax.jsglr2.tokens.Tokens;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoList;
import org.spoofax.terms.StrategoString;
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
            IObservableParser<?, ?> observableParser = (IObservableParser<?, ?>) jsglr2.parser;

            outputStream = outputStream();

            parse(jsglr2.parser);
//            System.out.println("aaaaaaaaa");
//            Tokens tokens = parseAndImplode(jsglr2);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void parse(IParser<?> parser) {
        StringBuilder input_content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(input)))) {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                input_content.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ParseResult<?> result = parser.parse(input_content.toString());

        if(result.isSuccess()) {
            ParseSuccess<?> success = (ParseSuccess<?>) result;

            // TODO: Probably check this cast
            recurse_tree((IParseNode) success.parseResult);

//            for (IDerivation derivation : ((HybridParseNode) success.parseResult).isAmbiguous()) {
//                System.out.println(derivation.descriptor());
//                System.out.println(derivation.production().constructor());
//                System.out.println(derivation.productionType().name());
//                System.out.println(derivation.width());
//                System.out.println();
//                System.out.println();
//            }

        } else {
            ParseFailure<?> failure = (ParseFailure<?>) result;
            System.out.println(failure.exception().getMessage());
        }
    }

    private void recurse_tree(IParseNode node) {
        IDerivation derivation = node.getFirstDerivation();
        for (IParseForest a : derivation.parseForests()) {
            if (a instanceof IParseNode) {
                IParseNode b = (IParseNode) a;
                System.out.println(b.descriptor());
                recurse_tree(b);
            } else {
                // TODO: Error
            }
        }
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

    private OutputStream outputStream() throws Exception {
        return System.out;
    }

}
