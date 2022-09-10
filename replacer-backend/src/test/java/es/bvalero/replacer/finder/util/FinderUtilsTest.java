package es.bvalero.replacer.finder.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo. Ra'anana. X2 Z";

        assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        assertTrue(FinderUtils.isWordCompleteInText(2, "hay", text));
        assertTrue(FinderUtils.isWordCompleteInText(6, "un", text));
        assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text));
        assertFalse(FinderUtils.isWordCompleteInText(15, "en", text));
        assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text));
        assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text));
        assertTrue(FinderUtils.isWordCompleteInText(31, "anana", text));
        assertFalse(FinderUtils.isWordCompleteInText(38, "X", text));
        assertTrue(FinderUtils.isWordCompleteInText(41, "Z", text));
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
        assertEquals(LinearMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 4));
        assertEquals(LinearMatchResult.of(8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 7));
        assertEquals(LinearMatchResult.of(5, "mundo"), FinderUtils.findWordAfter("Hola mundo", 4));
        assertEquals(LinearMatchResult.of(6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 4));
        assertEquals(LinearMatchResult.of(6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 5));
        assertEquals(LinearMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola-Mundo", 4));
        assertNull(FinderUtils.findWordAfter("Hola", 4));
        assertNull(FinderUtils.findWordAfter("Hola ", 4));
        assertEquals(LinearMatchResult.of(5, "A"), FinderUtils.findWordAfter("Hola A", 4));
        assertEquals(LinearMatchResult.of(6, "A"), FinderUtils.findWordAfter("Hola  A", 4));
        assertEquals(LinearMatchResult.of(7, "A"), FinderUtils.findWordAfter("Hola . A", 4, Set.of('.')));
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
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 5));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola-mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola", 0));
        assertNull(FinderUtils.findWordBefore("HolaXmundo", 5));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola  mundo", 6));
        assertEquals(LinearMatchResult.of(3, "Hola"), FinderUtils.findWordBefore("Un Hola, mundo", 9));
        assertEquals(LinearMatchResult.of(3, "Hola"), FinderUtils.findWordBefore("Un Hola , mundo", 10, Set.of(',')));
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
