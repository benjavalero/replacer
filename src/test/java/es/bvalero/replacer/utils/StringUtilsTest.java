package es.bvalero.replacer.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

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

    @Test
    public void testTrimText() {
        int threshold = 3;
        Pattern match = Pattern.compile("-");
        String text = "En un-lugar de-la Mancha de-cuyo-nombre no quiero acordarme.";
        String expected = "[...]  un-lug [...]  de-la  [...]  de-cuyo-nom [...]";
        Assert.assertEquals(expected, StringUtils.trimText(text, threshold, match));
    }

    @Test
    public void testRemoveParagraphsNotMatching() {
        String text = "A\n\nB\n\nC id=\"miss-2\"\n\nD id=\"miss-3\"\n\nE\n\nF id=\"miss-14\"\n\nG\n\nH\n\n";

        Pattern patterMatch = Pattern.compile("id=\"miss-[0-9]+\"");
        List<String> matchingParagraphs = StringUtils.removeParagraphsNotMatching(text, patterMatch);

        Assert.assertFalse(matchingParagraphs.isEmpty());
        Assert.assertEquals(3, matchingParagraphs.size());
        Assert.assertFalse(matchingParagraphs.contains("A"));
        Assert.assertFalse(matchingParagraphs.contains("B"));
        Assert.assertTrue(matchingParagraphs.contains("C id=\"miss-2\""));
        Assert.assertTrue(matchingParagraphs.contains("D id=\"miss-3\""));
        Assert.assertFalse(matchingParagraphs.contains("E"));
        Assert.assertTrue(matchingParagraphs.contains("F id=\"miss-14\""));
        Assert.assertFalse(matchingParagraphs.contains("G"));
        Assert.assertFalse(matchingParagraphs.contains("H"));
    }
}
