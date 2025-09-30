package es.bvalero.replacer.finder.util;

import static es.bvalero.replacer.finder.util.FinderUtils.END_TEMPLATE;
import static es.bvalero.replacer.finder.util.FinderUtils.START_TEMPLATE;
import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.finder.FinderPage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FinderUtilsTest {

    @Test
    void testStartsWithLowerCase() {
        assertFalse(FinderUtils.startsWithLowerCase("Álvaro"));
        assertTrue(FinderUtils.startsWithLowerCase("úlcera"));
        assertFalse(FinderUtils.startsWithLowerCase("1234"));
        assertFalse(FinderUtils.startsWithLowerCase(" 1234"));
    }

    @Test
    void testStartsWithUpperCase() {
        assertTrue(FinderUtils.startsWithUpperCase("Álvaro"));
        assertFalse(FinderUtils.startsWithUpperCase("úlcera"));
        assertFalse(FinderUtils.startsWithUpperCase("1234"));
        assertFalse(FinderUtils.startsWithUpperCase(" 1234"));
    }

    @Test
    void testStartsWithNumber() {
        assertFalse(FinderUtils.startsWithNumber("Álvaro"));
        assertFalse(FinderUtils.startsWithNumber("úlcera"));
        assertTrue(FinderUtils.startsWithNumber("1234"));
    }

    @Test
    void testSetFirstLowerCase() {
        assertEquals("álvaro", FinderUtils.setFirstLowerCase("Álvaro"));
        assertEquals("úlcera", FinderUtils.setFirstLowerCase("úlcera"));
        assertEquals("1234", FinderUtils.setFirstLowerCase("1234"));
        assertEquals("", FinderUtils.setFirstLowerCase(""));
    }

    @Test
    void testSetFirstUpperCaseClass() {
        assertEquals("[Aa]migo", FinderUtils.setFirstUpperCaseClass("amigo"));
        assertEquals("[Úú]lcera", FinderUtils.setFirstUpperCaseClass("úlcera"));
        assertEquals("1234", FinderUtils.setFirstUpperCaseClass("1234"));
    }

    @Test
    void testIsAscii() {
        assertTrue(FinderUtils.isAscii('A'));
        assertFalse(FinderUtils.isAscii('ñ'));
        assertFalse(FinderUtils.isAscii('1'));
    }

    @Test
    void testIsAsciiLowerCase() {
        assertTrue(FinderUtils.isAsciiLowerCase("amigo"));
        assertFalse(FinderUtils.isAsciiLowerCase("Amigo"));
        assertFalse(FinderUtils.isAsciiLowerCase("úlcera"));
        assertFalse(FinderUtils.isAsciiLowerCase("1234"));
    }

    @Test
    void testIsWordCompleteInText() {
        String text = "Y hay/un amigo en_mí mismo, Ra'anana. X2 Z. No-way. 1º. a[[link]]s.";

        assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        assertTrue(FinderUtils.isWordCompleteInText(2, "hay", text)); // Slash separator
        assertTrue(FinderUtils.isWordCompleteInText(6, "un", text)); // Slash separator
        assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text)); // Incomplete
        assertFalse(FinderUtils.isWordCompleteInText(15, "en", text)); // Underscore is a word char
        assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text)); // Underscore is a word char
        assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text)); // Comma separator
        assertTrue(FinderUtils.isWordCompleteInText(28, "Ra", text)); // Quote separator
        assertTrue(FinderUtils.isWordCompleteInText(31, "anana", text)); // Quote separator
        assertFalse(FinderUtils.isWordCompleteInText(38, "X", text)); // Digits are word chars
        assertTrue(FinderUtils.isWordCompleteInText(41, "Z", text)); // Dot separator
        assertTrue(FinderUtils.isWordCompleteInText(44, "No", text)); // Hyphen separator
        assertTrue(FinderUtils.isWordCompleteInText(47, "way", text)); // Hyphen separator
        assertTrue(FinderUtils.isWordCompleteInText(52, "1", text)); // Ordinals are not word chars
        assertTrue(FinderUtils.isWordCompleteInText(57, "[[link]]", text)); // Separators in the word itself
    }

    @Test
    void testIsWordFollowedByUpperCase() {
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola Mundo"));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(3, "Hola", "Un Hola Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola, Mundo"));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola-Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola_Mundo"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola"));
        assertFalse(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola "));
        assertTrue(FinderUtils.isWordFollowedByUpperCase(0, "Hola", "Hola A"));
    }

    @Test
    void testFindWordAfter() {
        assertEquals(FinderMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 4));
        assertEquals(FinderMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 5));
        assertEquals(FinderMatchResult.of(8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 7));
        assertEquals(FinderMatchResult.of(8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 8));
        assertEquals(FinderMatchResult.of(5, "mundo"), FinderUtils.findWordAfter("Hola mundo", 4));
        assertEquals(FinderMatchResult.of(6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 4));
        assertEquals(FinderMatchResult.of(6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 5));
        assertEquals(FinderMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola-Mundo", 4));
        assertNull(FinderUtils.findWordAfter("Hola", 4));
        assertNull(FinderUtils.findWordAfter("Hola ", 4));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordAfter("Hola", 0));
        assertEquals(FinderMatchResult.of(5, "A"), FinderUtils.findWordAfter("Hola A", 4));
        assertEquals(FinderMatchResult.of(6, "A"), FinderUtils.findWordAfter("Hola  A", 4));
        assertEquals(FinderMatchResult.of(7, "A"), FinderUtils.findWordAfter("Hola . A", 4));
        assertEquals(FinderMatchResult.of(5, "23.5"), FinderUtils.findWordAfter("Hola 23.5", 4, '.'));
        assertEquals(FinderMatchResult.of(10, "Mundo"), FinderUtils.findWordAfter("Hola&nbsp;Mundo", 4));
    }

    @Test
    void testCountWords() {
        assertEquals(0, FinderUtils.countWords("Mi casa es tu casa", 2, 3));
        assertEquals(1, FinderUtils.countWords("Mi casa es tu casa", 2, 8));
        assertEquals(2, FinderUtils.countWords("Mi casa es tu casa", 2, 11));
        assertEquals(3, FinderUtils.countWords("Mi casa es tu casa", 2, 14));
        assertEquals(4, FinderUtils.countWords("Mi casa es tu casa", 2, 18));
    }

    @Test
    void testIsWordPrecededByUpperCase() {
        assertTrue(FinderUtils.isWordPrecededByUpperCase(5, "Hola Mundo"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(8, "Un Hola Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "hola mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(6, "hola, Mundo"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(5, "Hola-Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "hola-Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(5, "Hola_Mundo"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(0, "Hola"));
        assertFalse(FinderUtils.isWordPrecededByUpperCase(1, " Hola"));
        assertTrue(FinderUtils.isWordPrecededByUpperCase(2, "A Hola"));
    }

    @Test
    void testFindWordBefore() {
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 5));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 4));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola-mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola", 0));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("HolaMundo", 4));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola", 4));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola  mundo", 6));
        assertEquals(FinderMatchResult.of(3, "Hola"), FinderUtils.findWordBefore("Un Hola, mundo", 9));
        assertEquals(FinderMatchResult.of(3, "Hola"), FinderUtils.findWordBefore("Un Hola , mundo", 10));
        assertEquals(FinderMatchResult.of(3, "23.5"), FinderUtils.findWordBefore("Un 23.5 , mundo", 10, '.'));
        assertEquals(FinderMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola&nbsp;mundo", 10));
    }

    @Test
    void testExpandRegex() {
        assertEquals(Set.of("a"), FinderUtils.expandRegex("a"));
        assertEquals(Set.of("abd", "acd"), FinderUtils.expandRegex("a[bc]d"));
        assertEquals(Set.of("abdf", "acdf", "abef", "acef"), FinderUtils.expandRegex("a[bc][de]f"));
        assertEquals(Set.of("ac", "abc"), FinderUtils.expandRegex("ab?c"));
        assertEquals(Set.of("abcd", "abd", "acd", "ad"), FinderUtils.expandRegex("ab?c?d"));
        assertEquals(Set.of("ad", "abd", "acd"), FinderUtils.expandRegex("a[bc]?d"));
    }

    @Test
    void testJoinAlternate() {
        assertEquals("a|b|c", FinderUtils.joinAlternate(List.of("a", "b", "c")));
        assertEquals("", FinderUtils.joinAlternate(List.of()));
        assertEquals("x", FinderUtils.joinAlternate(Set.of("x")));
    }

    /***** PARSE UTILS *****/

    private List<FinderMatchResult> findAllLinks(FinderPage page) {
        return new ArrayList<>(FinderUtils.findAllStructures(page, FinderUtils.START_LINK, FinderUtils.END_LINK));
    }

    @Test
    void testFindLink() {
        String link = "[[Link|Text]]";

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        assertEquals(1, matches.size());
        assertEquals(link, matches.get(0).group());
    }

    @Test
    void testFindLinkTruncated() {
        String link = "[[Link|Text";

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedLink() {
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link, link2);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLinks() {
        String link3 = "[[Link3|Text3]]";
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text %s Text]]", link2, link3);

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));

        FinderMatchResult matchLink = matches.stream().filter(m -> m.group().equals(link)).findAny().get();
        MatchResult matchLink2 = matches.stream().filter(m -> m.group().equals(link2)).findAny().get();
        MatchResult matchLink3 = matches.stream().filter(m -> m.group().equals(link3)).findAny().get();
        assertEquals(2, matchLink.groupCount());
        assertEquals(matchLink2, matchLink.groups().get(0));
        assertEquals(matchLink3, matchLink.groups().get(1));
        assertEquals(0, matchLink2.groupCount());
        assertEquals(0, matchLink3.groupCount());
    }

    @Test
    void testNestedLink2() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));

        FinderMatchResult matchLink = matches.stream().filter(m -> m.group().equals(link)).findAny().get();
        FinderMatchResult matchLink2 = matches.stream().filter(m -> m.group().equals(link2)).findAny().get();
        MatchResult matchLink3 = matches.stream().filter(m -> m.group().equals(link3)).findAny().get();
        assertEquals(1, matchLink.groupCount());
        assertEquals(matchLink2, matchLink.groups().getFirst());
        assertEquals(1, matchLink2.groupCount());
        assertEquals(matchLink3, matchLink2.groups().getFirst());
        assertEquals(0, matchLink3.groupCount());
    }

    @Test
    void testNestedLink2Truncated() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text", link2);

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLinkTruncated() {
        String link = "[[Link|Text [[Link2|Text2 Text]] [[Link3]]";

        FinderPage page = FinderPage.of(link);
        List<FinderMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of("[[Link2|Text2 Text]]", "[[Link3]]");
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testFindLinks() {
        String link1 = "[[Link1|Text1]]";
        String link2 = "[[Link2|Text2]]";
        String text = String.format("%s %s", link1, link2);

        FinderPage page = FinderPage.of(text);
        List<FinderMatchResult> matches = findAllLinks(page);

        List<String> expected = List.of(link1, link2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }

    private List<FinderMatchResult> findAllTemplates(FinderPage page) {
        return new ArrayList<>(FinderUtils.findAllStructures(page, START_TEMPLATE, END_TEMPLATE));
    }

    @Test
    void testFindTemplate() {
        String template = "{{Template|Text}}";

        FinderPage page = FinderPage.of(template);
        List<FinderMatchResult> matches = findAllTemplates(page);

        assertEquals(1, matches.size());
        assertEquals(template, matches.get(0).group());
    }

    @Test
    void testFindTemplateTruncated() {
        String template = "{{Template|Text";

        FinderPage page = FinderPage.of(template);
        List<FinderMatchResult> matches = findAllTemplates(page);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedTemplate() {
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text}}", template2);

        FinderPage page = FinderPage.of(template);
        List<FinderMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template, template2);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplates() {
        String template3 = "{{Template3|Text3}}";
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text %s Text}}", template2, template3);

        FinderPage page = FinderPage.of(template);
        List<FinderMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text}}", template2);

        FinderPage page = FinderPage.of(template);
        List<FinderMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2Truncated() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text", template2);

        FinderPage page = FinderPage.of(template);
        List<FinderMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testFindTemplates() {
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s", template1, template2);

        FinderPage page = FinderPage.of(text);
        List<FinderMatchResult> matches = findAllTemplates(page);

        List<String> expected = List.of(template1, template2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }

    @Test
    void testFindFakeTemplate() {
        String fake = "<math display=\"block\">Dw_{c,f,x}= {{\\sum(c*dw^2) \\over \\sum Dw}+10 \\over 2}</math>";
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s %s", fake, template1, template2);

        FinderPage page = FinderPage.of(text);
        List<FinderMatchResult> matches = findAllTemplates(page);

        Set<String> expected = Set.of(template1, template2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testFindAllWords() {
        // We consider word characters all letters and digits, and the underscore, but not the ordinals.
        String text = "A b/c-d_e,f.g'h[í]j\"1º2ª3°";

        Set<MatchResult> expected = new HashSet<>();
        expected.add(FinderMatchResult.of(0, "A"));
        expected.add(FinderMatchResult.of(2, "b"));
        expected.add(FinderMatchResult.of(4, "c"));
        expected.add(FinderMatchResult.of(6, "d_e"));
        expected.add(FinderMatchResult.of(10, "f"));
        expected.add(FinderMatchResult.of(12, "g"));
        expected.add(FinderMatchResult.of(14, "h"));
        expected.add(FinderMatchResult.of(16, "í"));
        expected.add(FinderMatchResult.of(18, "j"));
        expected.add(FinderMatchResult.of(20, "1"));
        expected.add(FinderMatchResult.of(22, "2"));
        expected.add(FinderMatchResult.of(24, "3"));

        List<MatchResult> matches = FinderUtils.findAllWords(text);
        assertEquals(expected, new HashSet<>(matches));
    }
}
