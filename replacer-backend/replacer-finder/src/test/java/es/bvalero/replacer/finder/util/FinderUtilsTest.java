package es.bvalero.replacer.finder.util;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.finder.util.FinderMatchRange;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FinderUtilsTest {

    @Test
    void testStartsWithLowerCase() {
        assertFalse(FinderUtils.startsWithLowerCase("Álvaro"));
        assertTrue(FinderUtils.startsWithLowerCase("úlcera"));
        assertFalse(FinderUtils.startsWithLowerCase("1234"));
        assertFalse(FinderUtils.startsWithLowerCase(" 1234"));
    }

    @Test
    void testStartsWithUpperCase() {
        assertTrue(FinderUtils.startsWithUpperCase("Álvaro"));
        assertFalse(FinderUtils.startsWithUpperCase("úlcera"));
        assertFalse(FinderUtils.startsWithUpperCase("1234"));
        assertFalse(FinderUtils.startsWithUpperCase(" 1234"));
    }

    @Test
    void testStartsWithNumber() {
        assertFalse(FinderUtils.startsWithNumber("Álvaro"));
        assertFalse(FinderUtils.startsWithNumber("úlcera"));
        assertTrue(FinderUtils.startsWithNumber("1234"));
    }

    @Test
    void testSetFirstLowerCase() {
        assertEquals("álvaro", FinderUtils.setFirstLowerCase("Álvaro"));
        assertEquals("úlcera", FinderUtils.setFirstLowerCase("úlcera"));
        assertEquals("1234", FinderUtils.setFirstLowerCase("1234"));
        assertEquals("", FinderUtils.setFirstLowerCase(""));
    }

    @Test
    void testSetFirstUpperCaseClass() {
        assertEquals("[Aa]migo", FinderUtils.setFirstUpperCaseClass("amigo"));
        assertEquals("[Úú]lcera", FinderUtils.setFirstUpperCaseClass("úlcera"));
        assertEquals("1234", FinderUtils.setFirstUpperCaseClass("1234"));
    }

    @Test
    void testIsAscii() {
        assertTrue(FinderUtils.isAscii('A'));
        assertFalse(FinderUtils.isAscii('ñ'));
        assertFalse(FinderUtils.isAscii('1'));
    }

    @Test
    void testIsAsciiLowerCase() {
        assertTrue(FinderUtils.isAsciiLowerCase("amigo"));
        assertFalse(FinderUtils.isAsciiLowerCase("Amigo"));
        assertFalse(FinderUtils.isAsciiLowerCase("úlcera"));
        assertFalse(FinderUtils.isAsciiLowerCase("1234"));
    }

    @Test
    void testIsLetter() {
        assertTrue(FinderUtils.isLetter('a'));
        assertTrue(FinderUtils.isLetter('Ü'));
        assertTrue(FinderUtils.isLetter('Ñ'));
        assertTrue(FinderUtils.isLetter('ç'));
        assertTrue(FinderUtils.isLetter(MASCULINE_ORDINAL));
        assertFalse(FinderUtils.isLetter(FEMININE_ORDINAL));
        assertFalse(FinderUtils.isLetter('2'));
        assertFalse(FinderUtils.isLetter(DEGREE));
        assertFalse(FinderUtils.isLetter(DOT));
        assertFalse(FinderUtils.isLetter('-'));
        assertFalse(FinderUtils.isLetter('_'));
    }

    @Test
    void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo, Ra'anana. X2 Z. No-way. 1º. a[[link]]s.";

        assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        assertTrue(FinderUtils.isWordCompleteInText(2, "hay", text)); // Slash separator
        assertTrue(FinderUtils.isWordCompleteInText(6, "un", text)); // Slash separator
        assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text)); // Incomplete
        assertFalse(FinderUtils.isWordCompleteInText(15, "en", text)); // Underscore is a word char
        assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text)); // Underscore is a word char
        assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text)); // Comma separator
        assertTrue(FinderUtils.isWordCompleteInText(28, "Ra", text)); // Quote separator
        assertTrue(FinderUtils.isWordCompleteInText(31, "anana", text)); // Quote separator
        assertFalse(FinderUtils.isWordCompleteInText(38, "X", text)); // Digits are word chars
        assertTrue(FinderUtils.isWordCompleteInText(41, "Z", text)); // Dot separator
        assertTrue(FinderUtils.isWordCompleteInText(44, "No", text)); // Hyphen separator
        assertTrue(FinderUtils.isWordCompleteInText(47, "way", text)); // Hyphen separator
        assertTrue(FinderUtils.isWordCompleteInText(52, "1", text)); // Ordinals are not word chars
        assertTrue(FinderUtils.isWordCompleteInText(57, "[[link]]", text)); // Separators in the word itself
    }

    @Test
    void testIsWordFollowedByUpperCase() {
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola Mundo"));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(3, "Hola", "Un Hola Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola, Mundo"));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola-Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola_Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola "));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola A"));
    }

    @Test
    void testIsEmptyBlankOrSpaceAlias() {
        assertTrue(FinderUtils.isEmptyBlankOrSpaceAlias("", 0, 0));
        assertTrue(FinderUtils.isEmptyBlankOrSpaceAlias("   ", 0, 3));
        assertTrue(FinderUtils.isEmptyBlankOrSpaceAlias("&nbsp;", 0, 6));
        assertTrue(FinderUtils.isEmptyBlankOrSpaceAlias("  &nbsp;  ", 0, 10));
        assertTrue(FinderUtils.isEmptyBlankOrSpaceAlias("&nbsp;&nbsp;", 0, 12));
        assertTrue(FinderUtils.isEmptyBlankOrSpaceAlias("  &nbsp;  &nbsp;  ", 0, 18));
        assertFalse(FinderUtils.isEmptyBlankOrSpaceAlias("text", 0, 4));
        assertFalse(FinderUtils.isEmptyBlankOrSpaceAlias("  text  ", 0, 8));
        assertFalse(FinderUtils.isEmptyBlankOrSpaceAlias("  &nbsp; text  ", 0, 15));
    }

    @Test
    void testIsBlank() {
        assertTrue(FinderUtils.isBlank("", 0, 0));
        assertTrue(FinderUtils.isBlank("   ", 0, 3));
        assertTrue(FinderUtils.isBlank("   ", 1, 2));
        assertFalse(FinderUtils.isBlank("text", 0, 4));
        assertFalse(FinderUtils.isBlank("  text  ", 0, 8));
        assertTrue(FinderUtils.isBlank("  text  ", 0, 2));
        assertTrue(FinderUtils.isBlank("  text  ", 6, 8));
        assertFalse(FinderUtils.isBlank("  text  ", 2, 6));
    }

    @Test
    void testFindWordAfter() {
        assertEquals(FinderMatchRange.of("Hola Mundo", 5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 4));
        assertEquals(FinderMatchRange.of("Hola Mundo", 5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 5));
        assertEquals(FinderMatchRange.of("Un Hola Mundo", 8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 7));
        assertEquals(FinderMatchRange.of("Un Hola Mundo", 8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 8));
        assertEquals(FinderMatchRange.of("Hola mundo", 5, "mundo"), FinderUtils.findWordAfter("Hola mundo", 4));
        assertEquals(FinderMatchRange.of("Hola, Mundo", 6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 4));
        assertEquals(FinderMatchRange.of("Hola, Mundo", 6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 5));
        assertEquals(FinderMatchRange.of("Hola-Mundo", 5, "Mundo"), FinderUtils.findWordAfter("Hola-Mundo", 4));
        assertNull(FinderUtils.findWordAfter("Hola", 4));
        assertNull(FinderUtils.findWordAfter("Hola ", 4));
        assertEquals(FinderMatchRange.of("Hola", 0, "Hola"), FinderUtils.findWordAfter("Hola", 0));
        assertEquals(FinderMatchRange.of("Hola A", 5, "A"), FinderUtils.findWordAfter("Hola A", 4));
        assertEquals(FinderMatchRange.of("Hola  A", 6, "A"), FinderUtils.findWordAfter("Hola  A", 4));
        assertEquals(FinderMatchRange.of("Hola . A", 7, "A"), FinderUtils.findWordAfter("Hola . A", 4));
        assertEquals(FinderMatchRange.of("Hola 23.5", 5, "23.5"), FinderUtils.findWordAfter("Hola 23.5", 4, '.'));
        assertEquals(
            FinderMatchRange.of("Hola&nbsp;Mundo", 10, "Mundo"),
            FinderUtils.findWordAfter("Hola&nbsp;Mundo", 4)
        );
    }

    @Test
    void testCountWords() {
        assertEquals(0, FinderUtils.countWords("Mi casa es tu casa", 2, 3));
        assertEquals(1, FinderUtils.countWords("Mi casa es tu casa", 2, 8));
        assertEquals(2, FinderUtils.countWords("Mi casa es tu casa", 2, 11));
        assertEquals(3, FinderUtils.countWords("Mi casa es tu casa", 2, 14));
        assertEquals(4, FinderUtils.countWords("Mi casa es tu casa", 2, 18));
    }

    @Test
    void testIsWordPrecededByUpperCase() {
        assertTrue(FinderUtils.isWordPrecededByUpperCase(5, "Hola Mundo"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(8, "Un Hola Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "hola mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(6, "hola, Mundo"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(5, "Hola-Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "hola-Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "Hola_Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(0, "Hola"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(1, " Hola"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(2, "A Hola"));
    }

    @Test
    void testFindWordBefore() {
        assertEquals(FinderMatchRange.of("Hola mundo", 0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 5));
        assertEquals(FinderMatchRange.of("Hola mundo", 0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 4));
        assertEquals(FinderMatchRange.of("Hola-mundo", 0, "Hola"), FinderUtils.findWordBefore("Hola-mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola", 0));
        assertEquals(FinderMatchRange.of("HolaMundo", 0, "Hola"), FinderUtils.findWordBefore("HolaMundo", 4));
        assertEquals(FinderMatchRange.of("Hola", 0, "Hola"), FinderUtils.findWordBefore("Hola", 4));
        assertEquals(FinderMatchRange.of("Hola  mundo", 0, "Hola"), FinderUtils.findWordBefore("Hola  mundo", 6));
        assertEquals(FinderMatchRange.of("Un Hola, mundo", 3, "Hola"), FinderUtils.findWordBefore("Un Hola, mundo", 9));
        assertEquals(
            FinderMatchRange.of("Un Hola , mundo", 3, "Hola"),
            FinderUtils.findWordBefore("Un Hola , mundo", 10)
        );
        assertEquals(
            FinderMatchRange.of("Un 23.5 , mundo", 3, "23.5"),
            FinderUtils.findWordBefore("Un 23.5 , mundo", 10, '.')
        );
        assertEquals(
            FinderMatchRange.of("Hola&nbsp;mundo", 0, "Hola"),
            FinderUtils.findWordBefore("Hola&nbsp;mundo", 10)
        );
    }

    @Test
    void testExpandRegex() {
        assertEquals(Set.of("a"), FinderUtils.expandRegex("a"));
        assertEquals(Set.of("abd", "acd"), FinderUtils.expandRegex("a[bc]d"));
        assertEquals(Set.of("abdf", "acdf", "abef", "acef"), FinderUtils.expandRegex("a[bc][de]f"));
        assertEquals(Set.of("ac", "abc"), FinderUtils.expandRegex("ab?c"));
        assertEquals(Set.of("abcd", "abd", "acd", "ad"), FinderUtils.expandRegex("ab?c?d"));
        assertEquals(Set.of("ad", "abd", "acd"), FinderUtils.expandRegex("a[bc]?d"));
    }

    @Test
    void testJoinAlternate() {
        assertEquals("a|b|c", FinderUtils.joinAlternate(List.of("a", "b", "c")));
        assertEquals("", FinderUtils.joinAlternate(List.of()));
        assertEquals("x", FinderUtils.joinAlternate(Set.of("x")));
    }

    @Test
    void testFindAllWords() {
        // We consider word characters all letters and digits, and the underscore, but not the ordinals.
        String text = "A b/c-d_e,f.g'h[í]j\"1º2ª3°";

        Set<MatchResult> expected = new HashSet<>();
        expected.add(FinderMatchRange.of(text, 0, "A"));
        expected.add(FinderMatchRange.of(text, 2, "b"));
        expected.add(FinderMatchRange.of(text, 4, "c"));
        expected.add(FinderMatchRange.of(text, 6, "d_e"));
        expected.add(FinderMatchRange.of(text, 10, "f"));
        expected.add(FinderMatchRange.of(text, 12, "g"));
        expected.add(FinderMatchRange.of(text, 14, "h"));
        expected.add(FinderMatchRange.of(text, 16, "í"));
        expected.add(FinderMatchRange.of(text, 18, "j"));
        expected.add(FinderMatchRange.of(text, 20, "1"));
        expected.add(FinderMatchRange.of(text, 22, "2"));
        expected.add(FinderMatchRange.of(text, 24, "3"));

        List<MatchResult> matches = FinderUtils.findAllWords(text);
        assertEquals(expected, new HashSet<>(matches));
    }
}
