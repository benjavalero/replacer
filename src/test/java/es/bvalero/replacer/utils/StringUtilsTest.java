package es.bvalero.replacer.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testEscapeText() {
        String text = "A \"B\" 'C' &D; <E> [F] : #G / «H» + “I”";
        Assert.assertEquals(text, StringUtils.unEscapeText(StringUtils.escapeText(text)));
    }

    @Test
    public void testReplaceAt() {
        String textReplaced = StringUtils.replaceAt("0123456789", 3, "34", "XXXX");
        Assert.assertNotNull(textReplaced);
        Assert.assertEquals("012XXXX56789", textReplaced);
    }

    @Test
    public void testReplaceAtModified() {
        Assert.assertNull(StringUtils.replaceAt("012XXXX56789", 3, "00", "XXXX"));
    }

    @Test
    public void testIsAllUppercase() {
        Assert.assertFalse(StringUtils.isAllUppercase(""));
        Assert.assertFalse(StringUtils.isAllUppercase("   "));
        Assert.assertFalse(StringUtils.isAllUppercase("cd"));
        Assert.assertTrue(StringUtils.isAllUppercase("CD"));
        Assert.assertFalse(StringUtils.isAllUppercase("CDs"));
        Assert.assertTrue(StringUtils.isAllUppercase("CO2"));
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

    @Test
    public void testTrimLeft() {
        int threshold = 5;
        String text = "Mi casa es bonita";
        String expected = "Mi ca [...]";
        Assert.assertEquals(expected, StringUtils.trimLeft(text, threshold));
    }

    @Test
    public void testTrimLeftNotModified() {
        int threshold = 5;
        String text = "Casas";
        String expected = "Casas";
        Assert.assertEquals(expected, StringUtils.trimLeft(text, threshold));
    }

    @Test
    public void testTrimRight() {
        int threshold = 5;
        String text = "Mi casa es bonita";
        String expected = "[...] onita";
        Assert.assertEquals(expected, StringUtils.trimRight(text, threshold));
    }


    @Test
    public void testTrimRightNotModified() {
        int threshold = 5;
        String text = "Casas";
        String expected = "Casas";
        Assert.assertEquals(expected, StringUtils.trimRight(text, threshold));
    }

    @Test
    public void testTrimLeftRight() {
        int threshold = 5;
        String text = "Mi casa es bonita";
        String expected = "Mi ca [...] onita";
        Assert.assertEquals(expected, StringUtils.trimLeftRight(text, threshold));
    }

    @Test
    public void testTrimLeftRightNotModified() {
        int threshold = 5;
        String text = "Mi casa es";
        String expected = "Mi casa es";
        Assert.assertEquals(expected, StringUtils.trimLeftRight(text, threshold));
    }

}
