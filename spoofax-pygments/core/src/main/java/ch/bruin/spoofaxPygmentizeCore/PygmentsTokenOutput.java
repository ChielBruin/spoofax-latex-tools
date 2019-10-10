package ch.bruin.spoofaxPygmentizeCore;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.core.style.StylerFacet;
import org.metaborg.spoofax.core.style.StylerFacetFromESV;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr2.JSGLR2Failure;
import org.spoofax.jsglr2.JSGLR2Success;
import org.spoofax.jsglr2.cli.JSGLR2CLI;
import org.spoofax.jsglr2.cli.WrappedException;
import org.spoofax.jsglr2.cli.output.IOutputProcessor;
import org.spoofax.jsglr2.cli.parserbuilder.SpoofaxLanguageFinder;
import org.spoofax.jsglr2.parser.result.ParseFailure;
import org.spoofax.jsglr2.parser.result.ParseSuccess;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class PygmentsTokenOutput implements IOutputProcessor {

    @Override
    public void checkAllowed(JSGLR2CLI cli) throws WrappedException {
        if (cli.input.length != 1)
            throw new WrappedException("Pygments token output is only supported for exactly one input.");
        if (!cli.implode)
            throw new WrappedException("The Spoofax Pygments integration only works with imploded ASTs.");
    }

    @Override
    public void outputParseResult(ParseSuccess<?> parseResult, PrintStream output) throws WrappedException {
        throw new WrappedException("The Spoofax Pygments integration only works with imploded ASTs.");
    }

    @Override
    public void outputParseFailure(ParseFailure<?> parseFailure, PrintStream output) throws WrappedException {
        throw new WrappedException("The Spoofax Pygments integration only works with imploded ASTs.");
    }

    @Override
    public void outputResult(JSGLR2Success<IStrategoTerm> result, PrintStream output) throws WrappedException {
        try {
            final InputStream esvInputStream;
            if (SpoofaxPygmentsCore.esvFile != null) {
                esvInputStream = new FileInputStream(SpoofaxPygmentsCore.esvFile);
            } else if (JSGLR2CLI.language != null) {
                ZipFile languageZip = new ZipFile(SpoofaxLanguageFinder.getSpoofaxLanguage(JSGLR2CLI.language));
                esvInputStream = languageZip.getInputStream(languageZip.getEntry("target/metaborg/editor.esv.af"));
            } else {
                esvInputStream = PygmentsTokenOutput.class.getResourceAsStream("/defaulteditor.esv.af");
            }

            final TermReader reader = new TermReader(new TermFactory().getFactoryWithStorageType(IStrategoTerm.IMMUTABLE));
            final IStrategoTerm esvTerm = reader.parseFromStream(esvInputStream);
            final StylerFacet stylerFacet = StylerFacetFromESV.create((IStrategoAppl) esvTerm);

            Map<IStrategoTerm, IStrategoTerm> parentMap = initParentMap(result.ast);

            Map<IToken, String> tokenMap = new LinkedHashMap<>();
            Map<String, IStyle> styleMap = new HashMap<>();
            styleMap.put("CustomToken", new Style(Color.BLACK, null, false, false, false, false));
            for (IToken token : result.tokens) {
                tokenMap.put(token, mapTokenToPygmentizeToken(token, stylerFacet, styleMap, parentMap));
            }

            JSONObject res = new JSONObject();

            res.put("styles", styleMap.entrySet().stream().map(e -> {
                IStyle style = e.getValue();
                Color bgColor = style.backgroundColor();
                Color color = style.color();
                return new ImmutablePair<>(e.getKey(),
                        (style.bold() ? "bold " : "") + (style.italic() ? "italic " : "")
                                + (style.underscore() ? "underline " : "") + (style.strikeout() ? "border:#000000 " : "")
                                + (bgColor == null ? "" : (String.format("bg:#%02x%02x%02x ", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue())))
                                + (color == null ? "" : (String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())))
                );
            }).collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue)));

            res.put("tokens", tokenMap.entrySet().stream()
                    .map(token -> {
                        JSONObject jsonToken = new JSONObject();
                        jsonToken.put("offset", token.getKey().getStartOffset());
                        jsonToken.put("tokenKind", token.getValue());
                        jsonToken.put("tokenString", token.getKey().toString());
                        return jsonToken;
                    })
                    .collect(Collectors.toList()));

            output.println(res.toString(2));
        } catch (IOException e) {
            throw new WrappedException("Could not read Spoofax language", e);
        }
    }

    @Override
    public void outputFailure(JSGLR2Failure<IStrategoTerm> result, PrintStream output) {
        output.println(new JSONObject().toString(2));
    }

    // BFS over all terms to build a parent map
    private Map<IStrategoTerm, IStrategoTerm> initParentMap(IStrategoTerm rootTerm) {
        Map<IStrategoTerm, IStrategoTerm> parentMap = new HashMap<>();
        Queue<IStrategoTerm> queue = new LinkedList<>();
        queue.add(rootTerm);
        while (!queue.isEmpty()) {
            IStrategoTerm term = queue.poll();
            for (IStrategoTerm subTerm : term.getAllSubterms()) {
                parentMap.put(subTerm, term);
                queue.add(subTerm);
            }
        }
        return parentMap;
    }

    @NotNull
    private String mapTokenToPygmentizeToken(IToken token, StylerFacet stylerFacet, Map<String, IStyle> styleMap,
                                             Map<IStrategoTerm, IStrategoTerm> parentMap) {
        final IStrategoTerm term = (IStrategoTerm) token.getAstNode();
        final IStrategoTerm parent = parentMap.get(term);
        if (term != null) {
            SortAndConstructor sac = SortAndConstructor.getFromTerm(stylerFacet, term, parent);
            if (sac != null) {
                if (stylerFacet.hasSortConsStyle(sac.sort, sac.constructor)) {
                    String name = "CustomSortCons" + sac.sort + sac.constructor;
                    styleMap.put(name, stylerFacet.sortConsStyle(sac.sort, sac.constructor));
                    return name;
                }
                if (stylerFacet.hasConsStyle(sac.constructor)) {
                    String name = "CustomCons" + sac.constructor;
                    styleMap.put(name, stylerFacet.consStyle(sac.constructor));
                    return name;
                }
                if (stylerFacet.hasSortStyle(sac.sort)) {
                    String name = "CustomSort" + sac.sort;
                    styleMap.put(name, stylerFacet.sortStyle(sac.sort));
                    return name;
                }
            }
        }
        String tokenName = mapTokenKindToString(token);
        if (tokenName != null && stylerFacet.hasTokenStyle("TK_" + tokenName)) {
            String name = "CustomToken" + tokenName;
            styleMap.put(name, stylerFacet.tokenStyle("TK_" + tokenName));
            return name;
        }
        return "CustomToken";
    }

    private String mapTokenKindToString(IToken token) {
        switch (token.getKind()) {
            case IToken.TK_IDENTIFIER:
                return "IDENTIFIER";
            case IToken.TK_NUMBER:
                return "NUMBER";
            case IToken.TK_STRING:
                return "STRING";
            case IToken.TK_ERROR_KEYWORD:
            case IToken.TK_KEYWORD:
                return "KEYWORD";
            case IToken.TK_OPERATOR:
                return "OPERATOR";
            case IToken.TK_VAR:
                return "VAR";
            case IToken.TK_ERROR_LAYOUT:
            case IToken.TK_LAYOUT:
                return "LAYOUT";
            default:
            case IToken.TK_UNKNOWN:
            case IToken.TK_ERROR:
            case IToken.TK_EOF:
            case IToken.TK_ERROR_EOF_UNEXPECTED:
            case IToken.TK_ESCAPE_OPERATOR:
            case IToken.TK_RESERVED:
            case IToken.TK_NO_TOKEN_KIND:
                return null;
        }
    }

}
