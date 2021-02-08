package es.bvalero.replacer.finder.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FinderUtilsTest {

    @Test
    void testStartsWithUpperCase() {
        Assertions.assertTrue(FinderUtils.startsWithUpperCase("Álvaro"));
        Assertions.assertFalse(FinderUtils.startsWithUpperCase("úlcera"));
        Assertions.assertFalse(FinderUtils.startsWithUpperCase("1234"));
    }

    @Test
    void testStartsWithLowerCase() {
        Assertions.assertFalse(FinderUtils.startsWithLowerCase("Álvaro"));
        Assertions.assertTrue(FinderUtils.startsWithLowerCase("úlcera"));
        Assertions.assertFalse(FinderUtils.startsWithLowerCase("1234"));
    }

    @Test
    void testSetFirstUpperCase() {
        Assertions.assertEquals("Álvaro", FinderUtils.setFirstUpperCase("Álvaro"));
        Assertions.assertEquals("Úlcera", FinderUtils.setFirstUpperCase("úlcera"));
        Assertions.assertEquals("1234", FinderUtils.setFirstUpperCase("1234"));
    }

    @Test
    void testSetFirstUpperCaseClass() {
        Assertions.assertEquals("[Aa]migo", FinderUtils.setFirstUpperCaseClass("amigo"));
        Assertions.assertEquals("[Úú]lcera", FinderUtils.setFirstUpperCaseClass("úlcera"));
    }

    @Test
    void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo. X";

        Assertions.assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        Assertions.assertFalse(FinderUtils.isWordCompleteInText(2, "hay", text));
        Assertions.assertFalse(FinderUtils.isWordCompleteInText(5, "un", text));
        Assertions.assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        Assertions.assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text));
        Assertions.assertFalse(FinderUtils.isWordCompleteInText(15, "en", text));
        Assertions.assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text));
        Assertions.assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text));
        Assertions.assertTrue(FinderUtils.isWordCompleteInText(28, "X", text));
    }
}
