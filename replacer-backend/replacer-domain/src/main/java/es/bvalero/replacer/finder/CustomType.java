package es.bvalero.replacer.finder;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomType that)) return false;
        if (!super.equals(o)) return false;
        return caseSensitive == that.caseSensitive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), caseSensitive);
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", this.getKind(), this.getSubtype(), this.caseSensitive);
    }
}
