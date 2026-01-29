package es.bvalero.replacer.finder.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ListingComparatorTest {

    private final ListingComparator cmp = new ListingComparator();

    @Test
    void testPrimaryOrdering() {
        // Primary ordering according to Spanish orthography

        // ñ is an independent letter
        assertTrue(cmp.comparePrimary("a", "b") < 0);
        assertTrue(cmp.comparePrimary("n", "ñ") < 0);
        assertTrue(cmp.comparePrimary("ñ", "o") < 0);

        // ch and ll are not letters and are sorted as ordinary sequences
        assertTrue(cmp.comparePrimary("cabo", "cacho") < 0);
        assertTrue(cmp.comparePrimary("cacho", "cada") < 0);
        assertTrue(cmp.comparePrimary("lana", "llano") < 0);
        assertTrue(cmp.comparePrimary("llano", "lona") < 0);

        // Letters with diacritics are not different letters
        assertTrue(cmp.comparePrimary("a", "á") == 0);
        assertTrue(cmp.comparePrimary("ú", "ü") == 0);

        // Uppercase letters are not different letters
        assertTrue(cmp.comparePrimary("a", "A") == 0);
        assertTrue(cmp.comparePrimary("á", "Á") == 0);
        assertTrue(cmp.comparePrimary("a", "Á") == 0);
        assertTrue(cmp.comparePrimary("ñ", "Ñ") == 0);

        // Spaces or dashes are ignored on the primary level
        assertTrue(cmp.comparePrimary("agua marina", "aguamarina") == 0);
        assertTrue(cmp.comparePrimary("teórico práctico", "teórico-práctico") == 0);

        // Shorter word comes before
        assertTrue(cmp.comparePrimary("pan", "panadero") < 0);
        assertTrue(cmp.comparePrimary("guión", "guionizar") < 0);
    }

    @Test
    void testSecondaryOrdering() {
        // ñ is an independent letter
        assertTrue(cmp.compareSecondary("a", "b") < 0);
        assertTrue(cmp.compareSecondary("n", "ñ") < 0);
        assertTrue(cmp.compareSecondary("ñ", "o") < 0);

        // ch and ll are not letters and are sorted as ordinary sequences
        assertTrue(cmp.compareSecondary("cabo", "cacho") < 0);
        assertTrue(cmp.compareSecondary("cacho", "cada") < 0);
        assertTrue(cmp.compareSecondary("lana", "llano") < 0);
        assertTrue(cmp.compareSecondary("llano", "lona") < 0);

        // NOTE: Letters with diacritics are different letters in the secondary level
        // The ordering matches the one of the Spanish dictionary,
        // where non-accented comes before accented, e.g. valido < válido, sabana < sábana
        assertTrue(cmp.compareSecondary("a", "á") < 0);
        assertTrue(cmp.compareSecondary("ú", "ü") != 0);

        // Uppercase letters are not different letters
        assertTrue(cmp.compareSecondary("a", "A") == 0);
        assertTrue(cmp.compareSecondary("á", "Á") == 0);
        assertTrue(cmp.compareSecondary("ñ", "Ñ") == 0);

        // NOTE: Spaces or dashes are not ignored on the secondary level
        assertTrue(cmp.compareSecondary("aguamarina", "agua marina") < 0);
        assertTrue(cmp.compareSecondary("teórico práctico", "teórico-práctico") < 0);

        // Shorter word comes before
        assertTrue(cmp.compareSecondary("pan", "panadero") < 0);
        assertTrue(cmp.compareSecondary("guión", "guionizar") < 0);
    }

    @Test
    void testTertiaryOrdering() {
        // ñ is an independent letter
        assertTrue(cmp.compareTertiary("a", "b") < 0);
        assertTrue(cmp.compareTertiary("n", "ñ") < 0);
        assertTrue(cmp.compareTertiary("ñ", "o") < 0);

        // ch and ll are not letters and are sorted as ordinary sequences
        assertTrue(cmp.compareTertiary("cabo", "cacho") < 0);
        assertTrue(cmp.compareTertiary("cacho", "cada") < 0);
        assertTrue(cmp.compareTertiary("lana", "llano") < 0);
        assertTrue(cmp.compareTertiary("llano", "lona") < 0);

        // NOTE: Letters with diacritics are different letters in the tertiary level
        // The ordering matches the one of the Spanish dictionary,
        // where non-accented comes before accented, e.g. valido < válido, sabana < sábana
        assertTrue(cmp.compareTertiary("a", "á") < 0);
        assertTrue(cmp.compareTertiary("ú", "ü") != 0);

        // NOTE: Uppercase letters are different letters in the tertiary level
        // The ordering matches the one of the Spanish dictionary,
        // where lowercase comes before uppercase, e.g. marta < Marta, marcial < Marcial
        assertTrue(cmp.compareTertiary("a", "A") < 0);
        assertTrue(cmp.compareTertiary("á", "Á") < 0);
        assertTrue(cmp.compareTertiary("ñ", "Ñ") < 0);

        // NOTE: Spaces or dashes are not ignored on the tertiary level
        assertTrue(cmp.compareTertiary("aguamarina", "agua marina") < 0);
        assertTrue(cmp.compareTertiary("teórico práctico", "teórico-práctico") < 0);

        // Shorter word comes before
        assertTrue(cmp.compareTertiary("pan", "panadero") < 0);
        assertTrue(cmp.compareTertiary("guión", "guionizar") < 0);
    }

    @Test
    void goldenTestSample() {
        List<String> golden = List.of(
            "adhesivo",
            "ad hoc",
            "ad hóminem",
            "ad honórem",
            "adhortar",
            "cana",
            "caña",
            "caos",
            "guión",
            "guionizar",
            "marcial",
            "Marcial",
            "marta",
            "Marta",
            "pan",
            "panadero",
            "sabana",
            "sábana",
            "valido",
            "válido"
        );

        // Same elements, deliberately shuffled
        List<String> shuffled = new ArrayList<>(
            List.of(
                "válido",
                "valido",
                "sábana",
                "sabana",
                "panadero",
                "pan",
                "Marta",
                "marta",
                "Marcial",
                "marcial",
                "guionizar",
                "guión",
                "caos",
                "caña",
                "cana",
                "adhortar",
                "ad honórem",
                "ad hóminem",
                "ad hoc",
                "adhesivo"
            )
        );

        shuffled.sort(cmp);

        assertEquals(golden, shuffled);
    }
}
