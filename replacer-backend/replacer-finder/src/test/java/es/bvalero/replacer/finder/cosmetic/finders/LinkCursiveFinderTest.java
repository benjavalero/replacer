package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LinkCursiveFinderTest {

    private LinkCursiveFinder linkCursiveFinder;

    @BeforeEach
    public void setUp() {
        linkCursiveFinder = new LinkCursiveFinder();
    }

    @ParameterizedTest
    @CsvSource(
        quoteCharacter = '"',
        value = {
            "[[''test'']], ''[[test]]''",
            "[[Test|''test'']], ''[[Test|test]]''",
            "[[Test|'''bold''']], '''[[Test|bold]]'''",
        }
    )
    void testLinkCursive(String text, String fix) {
        List<Cosmetic> cosmetics = linkCursiveFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "''[[test]]''",
            "[[''test]]''",
            "[[''Test''|test]]",
            "[[''Test|test'']]",
            // Sometimes there are complex cursive in the alias
            "[[Digitalis purpurea subsp. bocquetii|''D. purpurea'' subsp. ''bocquetii'']]",
        }
    )
    void testLinkCursiveNotValid(String text) {
        List<Cosmetic> cosmetics = linkCursiveFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
