package ch.bruin.spoofaxPygmentizeCore;

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
    public static SortAndConstructor getFromTerm(IStrategoTerm term, IStrategoTerm parent) {
        final int termType = term.getTermType();
        if (termType != IStrategoTerm.APPL && termType != IStrategoTerm.TUPLE && termType != IStrategoTerm.LIST) {
            // Try to use the parent of terminal nodes, mimicking behavior of old Spoofax/IMP runtime.
            if (parent != null) {
                final SortAndConstructor res = sortConsCategory(parent);
                if (res != null) {
                    return res;
                }
            }
        }
        return sortConsCategory(term);
    }

    public static SortAndConstructor sortConsCategory(IStrategoTerm term) {
        final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
        final String sort = imploderAttachment.getSort();
        if (sort == null) {
            return null;
        }
        // LEGACY: for some reason, when using concrete syntax extensions, all sorts are appended with _sort.
        final String massagedSort = sort.replace("_sort", "");
        if (term.getTermType() == IStrategoTerm.APPL) {
            final String cons = ((IStrategoAppl) term).getConstructor().getName();
            return new SortAndConstructor(massagedSort, cons);
        }
        return new SortAndConstructor(massagedSort, null);
    }
}
