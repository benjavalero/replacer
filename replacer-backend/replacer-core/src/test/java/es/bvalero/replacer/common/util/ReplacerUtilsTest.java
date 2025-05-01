package es.bvalero.replacer.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReplacerUtilsTest {

    @Test
    void testGetContextAroundWord() {
        String text = "En un lugar de la Mancha de cuyo nombre no quiero acordarme.";

        assertEquals("la Mancha de", ReplacerUtils.getContextAroundWord(text, 18, 24, 3));
        assertEquals("de la Mancha de cu", ReplacerUtils.getContextAroundWord(text, 18, 24, 6));
        assertEquals("En un lugar de la", ReplacerUtils.getContextAroundWord(text, 6, 11, 6));
        assertEquals("En un lugar de la Man", ReplacerUtils.getContextAroundWord(text, 6, 11, 10));
        assertEquals(" nombre no quiero acordarme.", ReplacerUtils.getContextAroundWord(text, 43, 49, 11));
        assertEquals("cuyo nombre no quiero acordarme.", ReplacerUtils.getContextAroundWord(text, 43, 49, 15));
    }

    @Test
    void testSetFirstUpperCase() {
        assertEquals("Álvaro", ReplacerUtils.setFirstUpperCaseIgnoringNonLetters("Álvaro"));
        assertEquals("Úlcera", ReplacerUtils.setFirstUpperCaseIgnoringNonLetters("úlcera"));
        assertEquals("1234", ReplacerUtils.setFirstUpperCaseIgnoringNonLetters("1234"));
        assertEquals("", ReplacerUtils.setFirstUpperCaseIgnoringNonLetters(""));
        assertEquals("A", ReplacerUtils.setFirstUpperCaseIgnoringNonLetters("a"));
        assertEquals("''Marketing''", ReplacerUtils.setFirstUpperCaseIgnoringNonLetters("''marketing''"));
    }

    @Test
    void testToUpperCase() {
        assertEquals("HOLA", ReplacerUtils.toUpperCase("HOLA"));
        assertEquals("¡HOLA, GAÑÁN!", ReplacerUtils.toUpperCase("¡Hola, gañán!"));
        assertEquals("1234", ReplacerUtils.toUpperCase("1234"));
    }
}
