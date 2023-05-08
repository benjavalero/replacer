package es.bvalero.replacer.common.domain;

import es.bvalero.replacer.finder.CustomMisspelling;
import lombok.Getter;

@Getter
public class CustomType extends ReplacementType {

    boolean caseSensitive;

    private CustomType(String subtype, boolean caseSensitive) {
        super(ReplacementKind.CUSTOM, subtype);
        this.caseSensitive = caseSensitive;
    }

    public static CustomType of(String replacement, boolean caseSensitive) {
        return new CustomType(replacement, caseSensitive);
    }

    public static CustomType of(CustomMisspelling customMisspelling) {
        return CustomType.of(customMisspelling.getWord(), customMisspelling.isCaseSensitive());
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", this.getKind(), this.getSubtype(), this.caseSensitive);
    }
}
