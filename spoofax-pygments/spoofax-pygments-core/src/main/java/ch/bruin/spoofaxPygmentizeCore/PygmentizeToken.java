package ch.bruin.spoofaxPygmentizeCore;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.parseforest.IParseNode;

public class PygmentizeToken {
    private int startIndex;
    private int endIndex;

    private String sort;
    private String constructor;
    private String value;

    public PygmentizeToken(int index, String sort, String constructor) {
        this.startIndex = index;
        this.endIndex = index;
        this.sort = sort;
        this.constructor = constructor;
    }

    public static PygmentizeToken fromParseNode(IParseNode node, int index) {
        IProduction production = node.production();
        production.isSkippableInParseForest();
        String sort = production.sort();
        String constructor = production.constructor();
        boolean isLex = production.isLexical();

        if (sort == null) {
            return null;
        } else {
            return new PygmentizeToken(index, sort, constructor);
        }
    }

    @Override
    public String toString() {
        return "(" + startIndex +
                ", " + endIndex +
                ", \"" + sort + '\"' +
                ", \"" + constructor + '\"' +
                ')';
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public void setEndIndex(int index) {
        this.endIndex = index;
    }
}
