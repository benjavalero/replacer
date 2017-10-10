package es.bvalero.replacer.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testEscapeText() {
        String text = "A \"B\" 'C' &D; <E> [F] : #G / «H» + “I”";
        Assert.assertEquals(text, StringUtils.unescapeText(StringUtils.escapeText(text)));
    }

    @Test
    public void testReplaceAt() {
        Assert.assertEquals("012XXXX56789",
                StringUtils.replaceAt("0123456789", 3, "34", "XXXX"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceAtModified() {
        Assert.assertEquals("012XXXX56789",
                StringUtils.replaceAt("012XXXX56789", 3, "00", "XXXX"));
    }

    @Test
    public void testStartsWithUpperCase() {
        Assert.assertTrue(StringUtils.startsWithUpperCase("Álvaro"));
        Assert.assertFalse(StringUtils.startsWithUpperCase("úlcera"));
    }

    @Test
    public void testSetFirstUpperCase() {
        Assert.assertEquals("Álvaro", StringUtils.setFirstUpperCase("Álvaro"));
        Assert.assertEquals("Úlcera", StringUtils.setFirstUpperCase("úlcera"));
        Assert.assertEquals("Ñ", StringUtils.setFirstUpperCase("ñ"));
    }


}
