package es.bvalero.replacer.finder.listing;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.VisibleForTesting;

public final class ListingComparator implements Comparator<String> {

    private static final Locale ES_LOCALE = Locale.forLanguageTag("es-ES");

    private static final Collator PRIMARY_COLLATOR;
    private static final Collator SECONDARY_COLLATOR;
    private static final Collator TERTIARY_COLLATOR;

    static {
        // We define different collator levels by setting the strength
        PRIMARY_COLLATOR = Collator.getInstance(ES_LOCALE);
        PRIMARY_COLLATOR.setStrength(Collator.PRIMARY);

        // Accents are taken into account
        SECONDARY_COLLATOR = Collator.getInstance(ES_LOCALE);
        SECONDARY_COLLATOR.setStrength(Collator.SECONDARY);

        // Lowercase and uppercase is taken into account
        TERTIARY_COLLATOR = Collator.getInstance(ES_LOCALE);
        TERTIARY_COLLATOR.setStrength(Collator.TERTIARY);
    }

    @Override
    public int compare(String a, String b) {
        if (Objects.equals(a, b)) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        // We can just use the tertiary level of collation to match the DLE behavior
        return TERTIARY_COLLATOR.compare(a, b);
    }

    @VisibleForTesting
    int comparePrimary(String a, String b) {
        // There is no need to normalize the words to apply the primary level of collation
        return PRIMARY_COLLATOR.compare(a, b);
    }

    @VisibleForTesting
    int compareSecondary(String a, String b) {
        return SECONDARY_COLLATOR.compare(a, b);
    }

    @VisibleForTesting
    int compareTertiary(String a, String b) {
        return TERTIARY_COLLATOR.compare(a, b);
    }
}
