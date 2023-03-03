package es.bvalero.replacer.common.domain;

import org.apache.commons.lang3.StringUtils;

public interface ReplacementType {
    int MAX_SUBTYPE_LENGTH = 100; // Constrained by the database

    ReplacementType NO_TYPE = new ReplacementType() {
        @Override
        public ReplacementKind getKind() {
            return ReplacementKind.EMPTY;
        }

        @Override
        public String getSubtype() {
            throw new IllegalCallerException();
        }

        @Override
        public String toString() {
            return "NO TYPE";
        }
    };

    ReplacementKind getKind();

    String getSubtype();

    static void validateSubtype(String subtype) {
        if (StringUtils.isBlank(subtype)) {
            throw new IllegalArgumentException("Invalid blank subtype for a standard type");
        }

        if (subtype.length() > MAX_SUBTYPE_LENGTH) {
            throw new IllegalArgumentException("Too long subtype: " + subtype);
        }
    }

    default boolean isStandardType() {
        return !isNoType() && !isCustomType();
    }

    default boolean isNoType() {
        return this.equals(NO_TYPE);
    }

    default boolean isCustomType() {
        return this.getKind() == ReplacementKind.CUSTOM;
    }

    default StandardType toStandardType() {
        assert this.isStandardType();
        assert this instanceof StandardType;
        return (StandardType) this;
    }

    default CustomType toCustomType() {
        assert this.isCustomType();
        assert this instanceof CustomType;
        return (CustomType) this;
    }
}
