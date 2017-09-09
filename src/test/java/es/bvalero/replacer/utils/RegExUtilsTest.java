package es.bvalero.replacer.utils;

import es.bvalero.replacer.domain.Interval;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class RegExUtilsTest {

    @Test
    public void testFindWords() {
        String text = "#hola-adiós. <!-- Comentario --> [[España, Francia]]. Hola, adiós.";
        Map<Integer, String> words = RegExUtils.findWords(text);
        assertEquals(7, words.size());
        assertTrue(words.containsValue("hola"));
        assertEquals("hola", words.get(1));
        assertTrue(words.containsValue("adiós"));
        assertEquals("adiós", words.get(6));
        assertEquals("adiós", words.get(60));
        assertTrue(words.containsValue("Comentario"));
        assertEquals("Comentario", words.get(18));
        assertTrue(words.containsValue("España"));
        assertEquals("España", words.get(35));
        assertTrue(words.containsValue("Francia"));
        assertEquals("Francia", words.get(43));
        assertTrue(words.containsValue("Hola"));
        assertEquals("Hola", words.get(54));
    }

    @Test
    public void testRegexWord() {
        String text = ":Españísima|";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_WORD);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("Españísima")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexTemplateParameter() {
        String text = "xxx {{Template| param1 = value1 | parám_ 2 = value2|param-3=|param4 }} {{Cita|Alea iacta est}} jajaja =";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_TEMPLATE_PARAM);
        Matcher matcher = pattern.matcher(text);
        Set<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }
        assertEquals(3, matches.size());
        assertTrue(matches.contains("| param1 ="));
        assertTrue(matches.contains("| parám_ 2 ="));
        assertTrue(matches.contains("|param-3="));
    }

    @Test
    public void testRegexParamValue() {
        String text = "xxx | índice = yyyy \n zzz|param=value|title  = Hola\n Adiós }} ttt";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_PARAM_VALUE);
        Matcher matcher = pattern.matcher(text);
        Set<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }
        assertEquals(1, matches.size());
        assertTrue(matches.contains("| índice = yyyy \n zzz"));
    }

    @Test
    public void testRegexUnreplacableTemplate() {
        String text = "xxx {{Cita| yyyy \n zzz }} ttt";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_UNREPLACEBLE_TEMPLATE);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("{{Cita| yyyy \n zzz }}")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexTemplateName() {
        String text = "xxx {{Plantilla| yyy }} zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_TEMPLATE_NAME);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("{{Plantilla")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexQuotes() {
        String text = "xxx '''I'm Muzzy''' \"zzz\" ''''ttt'' ''uuu\" vvv";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_QUOTES);
        Matcher matcher = pattern.matcher(text);
        Set<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }
        assertEquals(2, matches.size());
        assertTrue(matches.contains("'''I'm Muzzy'''"));
        assertTrue(matches.contains("''''ttt''"));
    }

    @Test
    public void testRegexQuotesEscaped() {
        String quotes = "''Hola''";
        String text = "xxx " + quotes + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_QUOTES_ESCAPED);
        Matcher matcher = pattern.matcher(StringUtils.escapeText(text));
        boolean isFound = false;
        while (matcher.find()) {
            if (StringUtils.unescapeText(matcher.group(0)).equals(quotes)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexQuotesAngular() {
        String text = "xxx «yyy» zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_ANGULAR_QUOTES);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("«yyy»")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexQuotesTypographic() {
        String text = "xxx “yyy” zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_TYPOGRAPHIC_QUOTES);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("“yyy”")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexQuotesDouble() {
        String text = "xxx \"yyy\" zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_DOUBLE_QUOTES);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("\"yyy\"")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexQuotesDoubleEscaped() {
        String quotes = "\"Hola\"";
        String text = "xxx " + quotes + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_DOUBLE_QUOTES_ESCAPED);
        Matcher matcher = pattern.matcher(StringUtils.escapeText(text));
        boolean isFound = false;
        while (matcher.find()) {
            if (StringUtils.unescapeText(matcher.group(0)).equals(quotes)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexFileName() {
        String text = "[[File: de_españa.png | España]] {{ X | co-co.svg | a = pepe.pdf }}";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_FILE_NAME);
        Matcher matcher = pattern.matcher(text);
        Set<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }
        assertEquals(3, matches.size());
        assertTrue(matches.contains(": de_españa.png"));
        assertTrue(matches.contains("= pepe.pdf"));
        assertTrue(matches.contains("| co-co.svg"));
    }

    @Test
    public void testRegexRefName() {
        String ref = "<ref  name= España >";
        String text = "xxx " + ref + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_REF_NAME);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals(ref)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexRefNameEscaped() {
        String ref = "<ref  name  =España />";
        String text = "xxx " + ref + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_REF_NAME_ESCAPED);
        Matcher matcher = pattern.matcher(StringUtils.escapeText(text));
        boolean isFound = false;
        while (matcher.find()) {
            if (StringUtils.unescapeText(matcher.group(0)).equals(ref)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexCategory() {
        String text = "xxx [[Categoría:Lluvia]] zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_CATEGORY);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals("[[Categoría:Lluvia]]")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexComment() {
        String text = "xxx <!-- Esto es un \n comentario --> zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_COMMENT, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).contains("<!-- Esto es un \n comentario -->")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexCommentEscaped() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_COMMENT_ESCAPED, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(StringUtils.escapeText(text));
        boolean isFound = false;
        while (matcher.find()) {
            if (StringUtils.unescapeText(matcher.group(0)).equals(comment)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexUrl() {
        String url = "https://google.es?u=t&ja2+rl=http://www.marca.com#page~2";
        String text = "xxx " + url + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_URL);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals(url)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexTagMath() {
        String math = "<math>Un <i>ejemplo</i>\n en LaTeX</math>";
        String text = "xxx " + math + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_TAG, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals(math)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexTagSource() {
        String math = "<source>Un <i>ejemplo</i>\n en LaTeX</source>";
        String text = "xxx " + math + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_TAG, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals(math)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexHeader() {
        String header = "== Esto es una cabecera ==";
        String text = "xxx " + header + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_HEADERS);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals(header)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testRegexWikilink() {
        String link = "[[Tales de Mileto|Thales de Mileto]]";
        String text = "xxx " + link + " zzz";
        Pattern pattern = Pattern.compile(RegExUtils.REGEX_WIKILINK);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = false;
        while (matcher.find()) {
            if (matcher.group(0).equals(link)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    @Test
    public void testFindExceptionIntervals() {
        String text = "xxx <!-- Comment --> zzz";
        List<Interval> intervals = RegExUtils.findExceptionIntervals(text);
        assertEquals(1, intervals.size());
        Interval interval1 = intervals.get(0);
        assertEquals("<!-- Comment -->", text.substring(interval1.getStart(), interval1.getEnd()));
    }

    @Test
    public void testLoadFalsePositives() {
        List<String> falsePositives = RegExUtils.loadFalsePositives();
        assertFalse(falsePositives.isEmpty());
        assertTrue(falsePositives.contains("Index"));
        assertTrue(falsePositives.contains("Magazine"));
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un Link de Éstas en el Index Online de ésta Tropicos.org.";
        Pattern pattern = Pattern.compile(RegExUtils.getRegexFalsePositives());
        Matcher matcher = pattern.matcher(text);
        Set<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }
        assertEquals(6, matches.size());
        assertTrue(matches.contains("Link"));
        assertTrue(matches.contains("Index"));
        assertTrue(matches.contains("Online"));
        assertTrue(matches.contains("Tropicos.org"));
        assertTrue(matches.contains("Éstas"));
        assertTrue(matches.contains("ésta"));
    }

    @Test
    public void testIsRedirectionArticle() {
        assertTrue(RegExUtils.isRedirectionArticle("xxx #REDIRECCIÓN [[A]] yyy"));
        assertTrue(RegExUtils.isRedirectionArticle("xxx #REDIRECT [[A]] yyy"));
        assertFalse(RegExUtils.isRedirectionArticle("Otro contenido"));
    }

}
