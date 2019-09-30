package ch.bruin.spoofaxPygmentizeCore;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.parseforest.IParseNode;

import java.util.ArrayList;
import java.util.List;

public class ExtendedPygmentizeToken {
    private int startIndex;
    private int endIndex;

    private String sort;
    private String constructor;
    private List<ExtendedPygmentizeToken> children;

    public ExtendedPygmentizeToken(int index, String sort, String constructor) {
        this.startIndex = index;
        this.endIndex = index;
        this.sort = sort;
        this.constructor = constructor;
        this.children = new ArrayList<>();
    }

    public static ExtendedPygmentizeToken fromParseNode(IParseNode node, int index) {
        IProduction production = node.production();
        production.isSkippableInParseForest();
        String sort = production.isLayout() ? "LAYOUT" : production.sort();
        String constructor = production.constructor();

        return new ExtendedPygmentizeToken(index, sort, constructor);
    }

    public void add_child(ExtendedPygmentizeToken child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return "{\"si\":" + startIndex +
                ", \"ei\":" + endIndex +
                ", \"s\": \"" + sort + '\"' +
                ", \"c\": \"" + constructor + '\"' +
                ", \"ch\":" + children +
                '}';
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public void setEndIndex(int index) {
        this.endIndex = index;
    }

    public void flatten(ArrayList<ExtendedPygmentizeToken> tokens) {
        tokens.add(this);
        for (ExtendedPygmentizeToken child : children) {
            child.flatten(tokens);
        }
    }
}
