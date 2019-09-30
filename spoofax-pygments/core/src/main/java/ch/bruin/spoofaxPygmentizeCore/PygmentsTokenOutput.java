package ch.bruin.spoofaxPygmentizeCore;

import com.google.common.collect.Streams;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2Failure;
import org.spoofax.jsglr2.JSGLR2Success;
import org.spoofax.jsglr2.cli.JSGLR2CLI;
import org.spoofax.jsglr2.cli.WrappedException;
import org.spoofax.jsglr2.cli.output.IOutputProcessor;
import org.spoofax.jsglr2.parseforest.ICharacterNode;
import org.spoofax.jsglr2.parseforest.IDerivation;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.IParseNode;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseSuccess;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PygmentsTokenOutput implements IOutputProcessor {

    @Override
    public void checkAllowed(JSGLR2CLI cli) throws WrappedException {
        if (cli.input.length != 1)
            throw new WrappedException("Pygments token output is only supported for exactly one input.");
    }

    // TODO remove support for parse without implode
    @Override
    public void outputParseResult(ParseSuccess<?> parseResult, PrintStream output) {
        ExtendedPygmentizeToken rootToken = new ExtendedPygmentizeToken(0, null, null);
        recurseTree((IParseNode) parseResult, 0, rootToken);
        calculateEndIndices(rootToken);
        output.println(rootToken);
    }

    @Override
    public void outputParseFailure(ParseFailure<?> parseFailure, PrintStream output) {
        output.print('[');
        output.print(new PygmentizeToken(0, "Token.Error", parseFailure.parseState.inputString));
        output.println(']');
    }

    @Override
    public void outputResult(JSGLR2Success<IStrategoTerm> result, PrintStream output) {
        output.print('[');
        output.print(Streams.stream(result.tokens).map(PygmentizeToken::new).map(PygmentizeToken::toString).collect(Collectors.joining(",")));
        output.println(']');
    }

    @Override
    public void outputFailure(JSGLR2Failure<IStrategoTerm> result, PrintStream output) {
        outputParseFailure(result.parseFailure, output);
    }

    private void calculateEndIndices(ExtendedPygmentizeToken root) {
        // TODO: We need to do a proper tree walk here, but this was quick to hack from the previous version
        ArrayList<ExtendedPygmentizeToken> tokens = new ArrayList<>();
        root.flatten(tokens);

        if (tokens.size() > 0) {
            for (int i = 0; i < tokens.size() - 1; i++) {
                ExtendedPygmentizeToken token = tokens.get(i);
                token.setEndIndex(Math.max(token.getStartIndex(), tokens.get(i + 1).getStartIndex()));
            }
            ExtendedPygmentizeToken token = tokens.get(tokens.size() - 1);
            token.setEndIndex(-1);
        }
    }

    private int recurseTree(IParseNode node, int index, ExtendedPygmentizeToken parent) {
        IDerivation derivation = node.getFirstDerivation();

        for (IParseForest pf : derivation.parseForests()) {
            if (pf instanceof IParseNode) {
                IParseNode parseNode = (IParseNode) pf;
                ExtendedPygmentizeToken token = ExtendedPygmentizeToken.fromParseNode(parseNode, index);
                parent.add_child(token);
                index = recurseTree(parseNode, index, token);
            } else if (pf instanceof ICharacterNode) {
                index++;
            } else {
                throw new IllegalArgumentException("Error on:" + pf.toString());
            }
        }
        return index;
    }
}
