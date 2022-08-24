package es.bvalero.replacer.finder.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FinderUtilsTest {

    @Test
    void testStartsWithUpperCase() {
        assertTrue(FinderUtils.startsWithUpperCase("Álvaro"));
        assertFalse(FinderUtils.startsWithUpperCase("úlcera"));
        assertFalse(FinderUtils.startsWithUpperCase("1234"));
    }

    @Test
    void testStartsWithLowerCase() {
        assertFalse(FinderUtils.startsWithLowerCase("Álvaro"));
        assertTrue(FinderUtils.startsWithLowerCase("úlcera"));
        assertFalse(FinderUtils.startsWithLowerCase("1234"));
    }

    @Test
    void testSetFirstUpperCase() {
        assertEquals("Álvaro", FinderUtils.setFirstUpperCase("Álvaro"));
        assertEquals("Úlcera", FinderUtils.setFirstUpperCase("úlcera"));
        assertEquals("1234", FinderUtils.setFirstUpperCase("1234"));
    }

    @Test
    void testSetFirstUpperCaseClass() {
        assertEquals("[Aa]migo", FinderUtils.setFirstUpperCaseClass("amigo"));
        assertEquals("[Úú]lcera", FinderUtils.setFirstUpperCaseClass("úlcera"));
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
    void testGetFirstWord() {
        assertEquals("", FinderUtils.getFirstWord(",. "));
        assertEquals("Text", FinderUtils.getFirstWord("Text"));
        assertEquals("Text", FinderUtils.getFirstWord("Text of text"));
        assertEquals("Text", FinderUtils.getFirstWord(" Text, of text"));
        assertEquals("11", FinderUtils.getFirstWord("11 texts"));
    }

    @Test
    void testFindWordBefore() {
        assertEquals("Hola", FinderUtils.findWordBefore("Hola mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola", 0));
        assertNull(FinderUtils.findWordBefore("HolaXmundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola  mundo", 6));
    }

    @Test
    void testFindWordAfter() {
        assertEquals("mundo", FinderUtils.findWordAfter("Hola mundo", 4));
        assertNull(FinderUtils.findWordAfter("Hola", 4));
        assertNull(FinderUtils.findWordAfter("HolaXmundo", 4));
        assertNull(FinderUtils.findWordAfter("Hola  mundo", 4));
    }
}
