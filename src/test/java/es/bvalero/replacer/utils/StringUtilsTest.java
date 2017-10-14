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

    @Test
    public void testTrimLeft() {
        int threshold = 5;
        String text = "Mi casa es bonita";
        String expected = "Mi ca [...]";
        Assert.assertEquals(expected, StringUtils.trimLeft(text, threshold));
    }

    @Test
    public void testTrimLeftWithSpan() {
        int threshold = 200;
        String text = "a Principios Del Siglo XVII: Un Recuento de Creencias Según Las Relaciones de Fe Del Tribunal de Cartagena de Indias |url=<span class=\"syntax exception\">http://books.google.com.mx/books?id=pxl4i7lM4IQC&amp;pg=PA102&amp;dq=giromancia&amp;hl=es&amp;sa=X&amp;ei=bCxjUoDjJOiUjAKjkID4AQ&amp;ved=0CD8Q6AEwAw#v=onepage&amp;q=giromancia&amp;f=false</span> |fechaacceso= |idioma= |otros= |edición= |año= 2011|editor= |editorial=Palibrio |ubicación= |isbn= 9781463305949 |capítulo= |páginas= |cita=}}<span class=\"syntax exception\">&lt;/ref&gt;</span> En la literatura se describen dos formas de practicar la giromancia.";
        String expected = "a Principios Del Siglo XVII: Un Recuento de Creencias Según Las Relaciones de Fe Del Tribunal de Cartagena de Indias |url=<span class=\"syntax exception\">http://books.google.com.mx/books?id=pxl4i7lM4IQC&amp;pg=PA102&amp;dq=giromancia&amp;hl=es&amp;sa=X&amp;ei=bCxjUoDjJOiUjAKjkID4AQ&amp;ved=0CD8Q6AEwAw#v=onepage&amp;q=giromancia&amp;f=false</span> [...]";
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
    public void testTrimRightWithSpan() {
        int threshold = 200;
        String text = "<span class=\"syntax exception\">&lt;ref&gt;</span>{{cita libro |apellido= C<span class=\"syntax exception\">&lt;small&gt;</span>RESPO<span class=\"syntax exception\">&lt;/small&gt;</span> V<span class=\"syntax exception\">&lt;small&gt;</span>ARGAS<span class=\"syntax exception\">&lt;/small&gt;</span>|nombre= Pablo L.|enlaceautor= |título=La Inquisicion Espanola Y Las Supersticiones en El Caribe ";
        String expected = "[...] <span class=\"syntax exception\">&lt;small&gt;</span>ARGAS<span class=\"syntax exception\">&lt;/small&gt;</span>|nombre= Pablo L.|enlaceautor= |título=La Inquisicion Espanola Y Las Supersticiones en El Caribe ";
        Assert.assertEquals(expected, StringUtils.trimRight(text, threshold));
    }

    @Test
    public void testTrimLeftRight() {
        int threshold = 5;
        String text = "Mi casa es bonita";
        String expected = "Mi ca [...] onita";
        Assert.assertEquals(expected, StringUtils.trimLeftRight(text, threshold));
    }

}
