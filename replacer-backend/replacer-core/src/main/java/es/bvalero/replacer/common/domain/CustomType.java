package es.bvalero.replacer.common.domain;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CustomType that = (CustomType) o;
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
