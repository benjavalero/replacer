package es.bvalero.replacer.finder.util;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.finder.FinderPage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FinderUtilsTest {

    @Test
    void testToLowerCase() {
        assertEquals("hola", FinderUtils.toLowerCase("hola"));
        assertEquals("¡hola, gañán!", FinderUtils.toLowerCase("¡Hola, GAÑÁN!"));
        assertEquals("1234", FinderUtils.toLowerCase("1234"));
    }

    @Test
    void testToUpperCase() {
        assertEquals("HOLA", FinderUtils.toUpperCase("HOLA"));
        assertEquals("¡HOLA, GAÑÁN!", FinderUtils.toUpperCase("¡Hola, gañán!"));
        assertEquals("1234", FinderUtils.toUpperCase("1234"));
    }

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
    void testSetFirstUpperCase() {
        assertEquals("Álvaro", FinderUtils.setFirstUpperCase("Álvaro"));
        assertEquals("Úlcera", FinderUtils.setFirstUpperCase("úlcera"));
        assertEquals("1234", FinderUtils.setFirstUpperCase("1234"));
        assertEquals("", FinderUtils.setFirstUpperCase(""));
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
        String text = "Y hay/un amigo en_mí mismo, Ra'anana. X2 Z. No-way.";

        assertTrue(FinderUtils.isWordCompleteInText(0, "Y", text));
        assertTrue(FinderUtils.isWordCompleteInText(2, "hay", text));
        assertTrue(FinderUtils.isWordCompleteInText(6, "un", text));
        assertTrue(FinderUtils.isWordCompleteInText(9, "amigo", text));
        assertFalse(FinderUtils.isWordCompleteInText(10, "migo", text));
        assertFalse(FinderUtils.isWordCompleteInText(15, "en", text));
        assertFalse(FinderUtils.isWordCompleteInText(18, "mí", text));
        assertTrue(FinderUtils.isWordCompleteInText(21, "mismo", text));
        assertTrue(FinderUtils.isWordCompleteInText(31, "anana", text));
        assertFalse(FinderUtils.isWordCompleteInText(38, "X", text));
        assertTrue(FinderUtils.isWordCompleteInText(41, "Z", text));
        assertTrue(FinderUtils.isWordCompleteInText(44, "No", text));
        assertTrue(FinderUtils.isWordCompleteInText(47, "way", text));
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
        assertEquals(LinearMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 4));
        assertEquals(LinearMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola Mundo", 5));
        assertEquals(LinearMatchResult.of(8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 7));
        assertEquals(LinearMatchResult.of(8, "Mundo"), FinderUtils.findWordAfter("Un Hola Mundo", 8));
        assertEquals(LinearMatchResult.of(5, "mundo"), FinderUtils.findWordAfter("Hola mundo", 4));
        assertEquals(LinearMatchResult.of(6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 4));
        assertEquals(LinearMatchResult.of(6, "Mundo"), FinderUtils.findWordAfter("Hola, Mundo", 5));
        assertEquals(LinearMatchResult.of(5, "Mundo"), FinderUtils.findWordAfter("Hola-Mundo", 4));
        assertNull(FinderUtils.findWordAfter("Hola", 4));
        assertNull(FinderUtils.findWordAfter("Hola ", 4));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordAfter("Hola", 0));
        assertEquals(LinearMatchResult.of(5, "A"), FinderUtils.findWordAfter("Hola A", 4));
        assertEquals(LinearMatchResult.of(6, "A"), FinderUtils.findWordAfter("Hola  A", 4));
        assertEquals(LinearMatchResult.of(7, "A"), FinderUtils.findWordAfter("Hola . A", 4));
        assertEquals(LinearMatchResult.of(5, "23.5"), FinderUtils.findWordAfter("Hola 23.5", 4, Set.of('.'), false));
        assertEquals(LinearMatchResult.of(5, "..."), FinderUtils.findWordAfter("Hola ...", 4, Set.of('.'), true));
        assertNull(FinderUtils.findWordAfter("Hola ...", 4, Set.of('.'), false));
        assertEquals(LinearMatchResult.of(10, "Mundo"), FinderUtils.findWordAfter("Hola&nbsp;Mundo", 4));
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
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 5));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola mundo", 4));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola-mundo", 5));
        assertNull(FinderUtils.findWordBefore("Hola", 0));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("HolaMundo", 4));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola", 4));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola  mundo", 6));
        assertEquals(LinearMatchResult.of(3, "Hola"), FinderUtils.findWordBefore("Un Hola, mundo", 9));
        assertEquals(LinearMatchResult.of(3, "Hola"), FinderUtils.findWordBefore("Un Hola , mundo", 10));
        assertEquals(
            LinearMatchResult.of(3, "23.5"),
            FinderUtils.findWordBefore("Un 23.5 , mundo", 10, Set.of('.'), false)
        );
        assertEquals(
            LinearMatchResult.of(3, "...."),
            FinderUtils.findWordBefore("Un .... , mundo", 10, Set.of('.'), true)
        );
        assertEquals(
            LinearMatchResult.of(6, "x"),
            FinderUtils.findWordBefore("Un ...x , mundo", 10, Set.of('.'), false)
        );
        assertNull(FinderUtils.findWordBefore("Un .... , mundo", 10, Set.of('.'), false));
        assertEquals(LinearMatchResult.of(0, "Hola"), FinderUtils.findWordBefore("Hola&nbsp;mundo", 10));
    }

    @Test
    void testJoinAlternate() {
        assertEquals("a|b|c", FinderUtils.joinAlternate(List.of("a", "b", "c")));
        assertEquals("", FinderUtils.joinAlternate(List.of()));
        assertEquals("x", FinderUtils.joinAlternate(Set.of("x")));
    }

    /***** PARSE UTILS *****/

    private List<LinearMatchResult> findAllLinks(FinderPage page) {
        return FinderUtils.findAllStructures(page, FinderUtils.START_LINK, FinderUtils.END_LINK);
    }

    @Test
    void testFindLink() {
        String link = "[[Link|Text]]";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        assertEquals(1, matches.size());
        assertEquals(link, matches.get(0).group());
    }

    @Test
    void testFindLinkTruncated() {
        String link = "[[Link|Text";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedLink() {
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link, link2);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLinks() {
        String link3 = "[[Link3|Text3]]";
        String link2 = "[[Link2|Text2]]";
        String link = String.format("[[Link|Text %s Text %s Text]]", link2, link3);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));

        LinearMatchResult matchLink = matches.stream().filter(m -> m.group().equals(link)).findAny().get();
        LinearMatchResult matchLink2 = matches.stream().filter(m -> m.group().equals(link2)).findAny().get();
        LinearMatchResult matchLink3 = matches.stream().filter(m -> m.group().equals(link3)).findAny().get();
        assertEquals(2, matchLink.getGroups().size());
        assertEquals(matchLink2, matchLink.getGroups().get(0));
        assertEquals(matchLink3, matchLink.getGroups().get(1));
        assertTrue(matchLink2.getGroups().isEmpty());
        assertTrue(matchLink3.getGroups().isEmpty());
    }

    @Test
    void testNestedLink2() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text]]", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link, link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));

        LinearMatchResult matchLink = matches.stream().filter(m -> m.group().equals(link)).findAny().get();
        LinearMatchResult matchLink2 = matches.stream().filter(m -> m.group().equals(link2)).findAny().get();
        LinearMatchResult matchLink3 = matches.stream().filter(m -> m.group().equals(link3)).findAny().get();
        assertEquals(1, matchLink.getGroups().size());
        assertEquals(matchLink2, matchLink.getGroups().get(0));
        assertEquals(1, matchLink2.getGroups().size());
        assertEquals(matchLink3, matchLink2.getGroups().get(0));
        assertTrue(matchLink3.getGroups().isEmpty());
    }

    @Test
    void testNestedLink2Truncated() {
        String link3 = "[[Link3|Text3]]";
        String link2 = String.format("[[Link2|Text2 %s Text2]]", link3);
        String link = String.format("[[Link|Text %s Text", link2);

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of(link2, link3);
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedLinkTruncated() {
        String link = "[[Link|Text [[Link2|Text2 Text]] [[Link3]]";

        FinderPage page = FinderPage.of(link);
        List<LinearMatchResult> matches = findAllLinks(page);

        Set<String> links = Set.of("[[Link2|Text2 Text]]", "[[Link3]]");
        assertEquals(links, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testFindLinks() {
        String link1 = "[[Link1|Text1]]";
        String link2 = "[[Link2|Text2]]";
        String text = String.format("%s %s", link1, link2);

        FinderPage page = FinderPage.of(text);
        List<LinearMatchResult> matches = findAllLinks(page);

        List<String> expected = List.of(link1, link2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }

    private List<LinearMatchResult> findAllTemplates(FinderPage page) {
        return FinderUtils.findAllStructures(page, "{{", "}}");
    }

    @Test
    void testFindTemplate() {
        String template = "{{Template|Text}}";

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = findAllTemplates(page);

        assertEquals(1, matches.size());
        assertEquals(template, matches.get(0).group());
    }

    @Test
    void testFindTemplateTruncated() {
        String template = "{{Template|Text";

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = findAllTemplates(page);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedTemplate() {
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text}}", template2);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template, template2);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplates() {
        String template3 = "{{Template3|Text3}}";
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text %s Text}}", template2, template3);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text}}", template2);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2Truncated() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text", template2);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = findAllTemplates(page);

        Set<String> templates = Set.of(template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testFindTemplates() {
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s", template1, template2);

        FinderPage page = FinderPage.of(text);
        List<LinearMatchResult> matches = findAllTemplates(page);

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
        List<LinearMatchResult> matches = findAllTemplates(page);

        Set<String> expected = Set.of(template1, template2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    public void testFindAllWords() {
        String text = "In the town where I_was born.";

        Set<MatchResult> expected = new HashSet<>();
        expected.add(LinearMatchResult.of(0, "In"));
        expected.add(LinearMatchResult.of(3, "the"));
        expected.add(LinearMatchResult.of(7, "town"));
        expected.add(LinearMatchResult.of(12, "where"));
        expected.add(LinearMatchResult.of(18, "I_was"));
        expected.add(LinearMatchResult.of(24, "born"));

        List<MatchResult> matches = FinderUtils.findAllWords(text);
        assertEquals(expected, new HashSet<>(matches));
    }
}
