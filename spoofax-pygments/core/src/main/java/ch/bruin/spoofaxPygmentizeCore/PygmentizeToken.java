package ch.bruin.spoofaxPygmentizeCore;

import org.metaborg.parsetable.productions.IProduction;
import org.spoofax.jsglr2.parseforest.IParseNode;

import java.util.ArrayList;
import java.util.List;

public class PygmentizeToken {
    private int startIndex;
    private int endIndex;

    private String sort;
    private String constructor;
    private List<PygmentizeToken> children;

    public PygmentizeToken(int index, String sort, String constructor) {
        this.startIndex = index;
        this.endIndex = index;
        this.sort = sort;
        this.constructor = constructor;
        this.children = new ArrayList<>();
    }

    public static PygmentizeToken fromParseNode(IParseNode node, int index) {
        IProduction production = node.production();
        production.isSkippableInParseForest();
        String sort = production.isLayout() ? "LAYOUT" : production.sort();
        String constructor = production.constructor();

        return new PygmentizeToken(index, sort, constructor);
    }

    public void add_child(PygmentizeToken child) {
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

    public void flatten(ArrayList<PygmentizeToken> tokens) {
        tokens.add(this);
        for (PygmentizeToken child : children) {
            child.flatten(tokens);
        }
    }
}
