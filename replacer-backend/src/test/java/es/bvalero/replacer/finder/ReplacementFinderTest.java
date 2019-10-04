package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

public class ReplacementFinderTest extends BaseReplacementFinder {

    @Test
    public void testStartsWithUpperCase() {
        Assert.assertTrue(startsWithUpperCase("Álvaro"));
        Assert.assertFalse(startsWithUpperCase("úlcera"));
        Assert.assertFalse(startsWithUpperCase("1234"));
    }

    @Test
    public void testStartsWithLowerCase() {
        Assert.assertFalse(startsWithLowerCase("Álvaro"));
        Assert.assertTrue(startsWithLowerCase("úlcera"));
        Assert.assertFalse(startsWithLowerCase("1234"));
    }

    @Test
    public void testSetFirstUpperCase() {
        Assert.assertEquals("Álvaro", setFirstUpperCase("Álvaro"));
        Assert.assertEquals("Úlcera", setFirstUpperCase("úlcera"));
        Assert.assertEquals("1234", setFirstUpperCase("1234"));
    }

    @Test
    public void testSetFirstUpperCaseClass() {
        Assert.assertEquals("[Aa]migo", setFirstUpperCaseClass("amigo"));
        Assert.assertEquals("[Úú]lcera", setFirstUpperCaseClass("úlcera"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFirstUpperCaseClassException() {
        setFirstUpperCaseClass("Álvaro");
    }

    @Test
    public void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo.";

        Assert.assertTrue(isWordCompleteInText(0, "Y", text));
        Assert.assertFalse(isWordCompleteInText(2, "hay", text));
        Assert.assertFalse(isWordCompleteInText(5, "un", text));
        Assert.assertTrue(isWordCompleteInText(9, "amigo", text));
        Assert.assertFalse(isWordCompleteInText(10, "migo", text));
        Assert.assertFalse(isWordCompleteInText(15, "en", text));
        Assert.assertFalse(isWordCompleteInText(18, "mí", text));
        Assert.assertTrue(isWordCompleteInText(21, "mismo", text));
    }

}
