package es.bvalero.replacer.finder.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinderUtilsTest {

    @Test
    void testToLowerCase() {
        assertEquals("hola", FinderUtils.toLowerCase("hola"));
        assertEquals("¡hola, gañán!", FinderUtils.toLowerCase("¡Hola, GAÑÁN!"));
        assertEquals("1234", FinderUtils.toLowerCase("1234"));
    }

    @Test
    void testToUpperCase() {
        assertEquals("HOLA", FinderUtils.toUpperCase("HOLA"));
        assertEquals("¡HOLA, GAÑÁN!", FinderUtils.toUpperCase("¡Hola, gañán!"));
        assertEquals("1234", FinderUtils.toUpperCase("1234"));
    }

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
    void testSetFirstUpperCase() {
        assertEquals("Álvaro", FinderUtils.setFirstUpperCase("Álvaro"));
        assertEquals("Úlcera", FinderUtils.setFirstUpperCase("úlcera"));
        assertEquals("1234", FinderUtils.setFirstUpperCase("1234"));
        assertEquals("", FinderUtils.setFirstUpperCase(""));
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
    void testIsUpperCase() {
        assertTrue(FinderUtils.isUpperCase("ESPAÑA"));
        assertFalse(FinderUtils.isUpperCase("España"));
        assertFalse(FinderUtils.isUpperCase("1234"));
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
    void testIsWord() {
        assertTrue(FinderUtils.isWord("España"));
        assertFalse(FinderUtils.isWord("Hola Mundo"));
        assertFalse(FinderUtils.isWord("word1234"));
    }

    @Test
    void testIsNumber() {
        assertTrue(FinderUtils.isNumber("1234"));
        assertFalse(FinderUtils.isNumber("12 34"));
        assertFalse(FinderUtils.isNumber("word1234"));
    }

    @Test
    void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo. Ra'anana. X";

        assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        assertFalse(FinderUtils.isWordCompleteInText(2, "hay", text));
        assertFalse(FinderUtils.isWordCompleteInText(5, "un", text));
        assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text));
        assertFalse(FinderUtils.isWordCompleteInText(15, "en", text));
        assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text));
        assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text));
        assertFalse(FinderUtils.isWordCompleteInText(31, "anana", text));
        assertTrue(FinderUtils.isWordCompleteInText(38, "X", text));
    }

    @Test
    void testIsWordFollowedByUpperCase() {
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola Mundo"));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(3, "Hola", "Un Hola Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola, Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola-Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola "));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola A"));
    }

    @Test
    void testFindWordAfter() {
        assertEquals("Mundo", FinderUtils.findWordAfter(0, "Hola", "Hola Mundo"));
        assertEquals("Mundo", FinderUtils.findWordAfter(3, "Hola", "Un Hola Mundo"));
        assertEquals("mundo", FinderUtils.findWordAfter(0, "Hola", "Hola mundo"));
        assertNull(FinderUtils.findWordAfter(0, "Hola", "Hola, Mundo"));
        assertNull(FinderUtils.findWordAfter(0, "Hola", "Hola-Mundo"));
        assertNull(FinderUtils.findWordAfter(0, "Hola", "Hola"));
        assertNull(FinderUtils.findWordAfter(0, "Hola", "Hola "));
        assertEquals("A", FinderUtils.findWordAfter(0, "Hola", "Hola A"));
    }

    @Test
    void testIsWordPrecededByUpperCase() {
        assertTrue(FinderUtils.isWordPrecededByUpperCase(5, "Hola Mundo"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(8, "Un Hola Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "hola mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(6, "hola, Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "hola-Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(0, "Hola"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(1, " Hola"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(2, "A Hola"));
    }

    @Test
    void testFindWordBefore() {
        assertEquals("Hola", FinderUtils.findWordBefore("Hola mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola-mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola", 0));
        assertNull(FinderUtils.findWordBefore("HolaXmundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola  mundo", 6));
    }

    @Test
    void testFindFirstWord() {
        assertNull(FinderUtils.findFirstWord(",. "));
        assertEquals("Text", FinderUtils.findFirstWord("* Text"));
        assertEquals("Text", FinderUtils.findFirstWord("Text"));
        assertEquals("Text", FinderUtils.findFirstWord("Text of text"));
        assertEquals("Text", FinderUtils.findFirstWord(" Text, of text"));
        assertEquals("11", FinderUtils.findFirstWord("11 texts"));
    }

    @Test
    void testJoinAlternate() {
        assertEquals("a|b|c", FinderUtils.joinAlternate(List.of("a", "b", "c")));
        assertEquals("", FinderUtils.joinAlternate(Collections.emptyList()));
        assertEquals("x", FinderUtils.joinAlternate(Collections.singleton("x")));
    }
}
