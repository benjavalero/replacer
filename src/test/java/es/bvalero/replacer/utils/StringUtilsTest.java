package es.bvalero.replacer.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testEscapeText() {
        String text = "A \"B\" 'C' &D; <E> [F] : #G / «H» + “I”";
        assertEquals(text, StringUtils.unescapeText(StringUtils.escapeText(text)));
    }

    @Test
    public void testReplaceAt() throws Exception {
        assertEquals("012XXXX56789",
                StringUtils.replaceAt("0123456789", 3, "34", "XXXX"));
    }

    @Test(expected = Exception.class)
    public void testReplaceAtModified() throws Exception {
        assertEquals("012XXXX56789",
                StringUtils.replaceAt("0123456789", 3, "00", "XXXX"));
    }

    @Test
    public void testStartsWithUpperCase() {
        assertTrue(StringUtils.startsWithUpperCase("Álvaro"));
        assertFalse(StringUtils.startsWithUpperCase("úlcera"));
    }

    @Test
    public void testSetFirstUpperCase() {
        assertEquals("Álvaro", StringUtils.setFirstUpperCase("Álvaro"));
        assertEquals("Úlcera", StringUtils.setFirstUpperCase("úlcera"));
    }

    @Test
    public void testRemoveParagraphsWithoutMisspellings() {
        String text = "A\n\nB\n\nC miss-2\n\nD miss-3\n\nE\n\nF miss-4\n\nG\n\nH\n\n";
        String expected = "C miss-2\n<hr>\nD miss-3\n<hr>\nF miss-4";
        assertEquals(expected, StringUtils.removeParagraphsWithoutMisspellings(text));
    }

    @Test
    public void testTrimText() {
        String text = "Es una casa muy bonita <button>xxx</button> con vistas al mar, solárium en la terraza <button>zzz</button> y minibar en el sótano.";
        String expected = "[...] uy bonita <button>xxx</button> con vista [...] a terraza <button>zzz</button> y minibar [...]";
        assertEquals(expected, StringUtils.trimText(text, 10));
    }

    @Test
    public void testHighlightComment() {
        String text = "xxx &lt;!-- Un comentario --&gt; zzz";
        String expected = "xxx <span class=\"syntax comment\">&lt;!-- Un comentario --&gt;</span> zzz";
        assertEquals(expected, StringUtils.highlightSyntax(text));
    }

    @Test
    public void testHighlightHeader() {
        String text = "xxx == Una cabecera == zzz";
        String expected = "xxx <span class=\"syntax header\">== Una cabecera ==</span> zzz";
        assertEquals(expected, StringUtils.highlightSyntax(text));
    }

    @Test
    public void testHighlightHyperlink() {
        String text = "xxx https://es.wikipedia.org zzz";
        String expected = "xxx <span class=\"syntax hyperlink\">https://es.wikipedia.org</span> zzz";
        assertEquals(expected, StringUtils.highlightSyntax(text));
    }

    @Test
    public void testHighlightWikilink() {
        String text = "xxx [[España]] zzz";
        String expected = "xxx <span class=\"syntax link\">[[España]]</span> zzz";
        assertEquals(expected, StringUtils.highlightSyntax(text));
    }

}
