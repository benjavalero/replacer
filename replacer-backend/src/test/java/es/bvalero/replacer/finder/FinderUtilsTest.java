package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

public class FinderUtilsTest {

    @Test
    public void testStartsWithUpperCase() {
        Assert.assertTrue(FinderUtils.startsWithUpperCase("Álvaro"));
        Assert.assertFalse(FinderUtils.startsWithUpperCase("úlcera"));
        Assert.assertFalse(FinderUtils.startsWithUpperCase("1234"));
    }

    @Test
    public void testStartsWithLowerCase() {
        Assert.assertFalse(FinderUtils.startsWithLowerCase("Álvaro"));
        Assert.assertTrue(FinderUtils.startsWithLowerCase("úlcera"));
        Assert.assertFalse(FinderUtils.startsWithLowerCase("1234"));
    }

    @Test
    public void testSetFirstUpperCase() {
        Assert.assertEquals("Álvaro", FinderUtils.setFirstUpperCase("Álvaro"));
        Assert.assertEquals("Úlcera", FinderUtils.setFirstUpperCase("úlcera"));
        Assert.assertEquals("1234", FinderUtils.setFirstUpperCase("1234"));
    }

    @Test
    public void testSetFirstUpperCaseClass() {
        Assert.assertEquals("[Aa]migo", FinderUtils.setFirstUpperCaseClass("amigo"));
        Assert.assertEquals("[Úú]lcera", FinderUtils.setFirstUpperCaseClass("úlcera"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFirstUpperCaseClassException() {
        FinderUtils.setFirstUpperCaseClass("Álvaro");
    }

    @Test
    public void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo. X";

        Assert.assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        Assert.assertFalse(FinderUtils.isWordCompleteInText(2, "hay", text));
        Assert.assertFalse(FinderUtils.isWordCompleteInText(5, "un", text));
        Assert.assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        Assert.assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text));
        Assert.assertFalse(FinderUtils.isWordCompleteInText(15, "en", text));
        Assert.assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text));
        Assert.assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text));
        Assert.assertTrue(FinderUtils.isWordCompleteInText(28, "X", text));
    }
}
