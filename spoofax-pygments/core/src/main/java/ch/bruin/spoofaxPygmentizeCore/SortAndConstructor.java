package ch.bruin.spoofaxPygmentizeCore;

import org.metaborg.spoofax.core.style.StylerFacet;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

public final class SortAndConstructor {
    public final String sort;
    public final String constructor;

    public SortAndConstructor(String sort, String constructor) {
        this.sort = sort;
        this.constructor = constructor;
    }

    // Copied partially from spoofax-releng/spoofax/org.metaborg.spoofax.core/src/main/java/org/metaborg/spoofax/core/style/CategorizerService.java
    public static SortAndConstructor getFromTerm(StylerFacet facet, IStrategoTerm term, IStrategoTerm parentTerm) {
        if (term == null) {
            return null;
        }

        final int termType = term.getTermType();
        if (termType != IStrategoTerm.APPL && termType != IStrategoTerm.TUPLE && termType != IStrategoTerm.LIST) {
            // Try to use the parent of terminal nodes, mimicking behavior of old Spoofax/IMP runtime.
            if (parentTerm != null) {
                final SortAndConstructor category = sortConsCategory(facet, parentTerm);
                if (category != null) {
                    return category;
                }
            }
        }

        return sortConsCategory(facet, term);
    }

    private static SortAndConstructor sortConsCategory(StylerFacet facet, IStrategoTerm term) {
        final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
        final String sort = imploderAttachment.getSort();
        if (sort == null) {
            return null;
        }
        // LEGACY: for some reason, when using concrete syntax extensions, all sorts are appended with _sort.
        final String massagedSort = sort.replace("_sort", "");
        if (term.getTermType() == IStrategoTerm.APPL) {
            final String cons = ((IStrategoAppl) term).getConstructor().getName();
            if (facet.hasSortConsStyle(massagedSort, cons) || facet.hasConsStyle(cons) || facet.hasSortStyle(massagedSort)) {
                return new SortAndConstructor(massagedSort, cons);
            }
            return null;
        }

        if (facet.hasSortStyle(massagedSort)) {
            return new SortAndConstructor(massagedSort, null);
        }

        return null;
    }
}
