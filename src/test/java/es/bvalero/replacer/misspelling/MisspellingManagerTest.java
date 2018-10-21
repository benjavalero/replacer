package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class MisspellingManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManagerTest.class);

    @Mock
    private IWikipediaFacade wikipediaService;

    @InjectMocks
    private MisspellingManager misspellingManager;

    @Before
    public void setUp() {
        misspellingManager = new MisspellingManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindWikipediaMisspellingsWithErrors() throws WikipediaException {
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenThrow(new WikipediaException());
        Assert.assertTrue(misspellingManager.findWikipediaMisspellings().isEmpty());
    }

    @Test
    public void testParseMisspellingListText() {
        String misspellingListText = "Texto\n\n" +
                "A||B\n" + // No starting whitespace
                " C|cs|D\n" +
                " E|CS|F\n" +
                " G|H\n" + // Bad formatted
                " I||J\n" +
                " k||k (letra), que, qué, kg (kilogramo)\n" +
                " I||J\n" + // Duplicated
                " renuncio||renunció (3.ª persona), renuncio (1.ª persona)\n" +
                " remake||(nueva) versión o adaptación\n" +
                " desempeño||desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)";

        Collection<Misspelling> misspellings = MisspellingManager.parseMisspellingListText(misspellingListText);
        Assert.assertEquals(7, misspellings.size());

        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("C").setCaseSensitive(true).setComment("D").build()));
        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("E").setCaseSensitive(true).setComment("F").build()));
        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("I").setCaseSensitive(false).setComment("J").build()));
    }

    @Test
    public void testSetFirstUpperCase() {
        Assert.assertEquals("Álvaro", MisspellingManager.setFirstUpperCase("Álvaro"));
        Assert.assertEquals("Úlcera", MisspellingManager.setFirstUpperCase("úlcera"));
        Assert.assertEquals("Ñ", MisspellingManager.setFirstUpperCase("ñ"));
    }

    @Test
    public void testStartsWithUpperCase() {
        Assert.assertTrue(MisspellingManager.startsWithUpperCase("Álvaro"));
        Assert.assertFalse(MisspellingManager.startsWithUpperCase("úlcera"));
    }

    @Test
    public void testBuildMisspellingMap() {
        Misspelling misspelling1 =
                Misspelling.builder().setWord("haver").setComment("haber").setCaseSensitive(false).build();
        Misspelling misspelling2 =
                Misspelling.builder().setWord("madrid").setComment("Madrid").setCaseSensitive(true).build();
        List<Misspelling> misspellingList = Arrays.asList(misspelling1, misspelling2);

        Map<String, Misspelling> misspellingMap = MisspellingManager.buildMisspellingMap(misspellingList);

        Assert.assertEquals(3, misspellingMap.size());
        Assert.assertEquals(misspelling1, misspellingMap.get("haver"));
        Assert.assertEquals(misspelling1, misspellingMap.get("Haver"));
        Assert.assertEquals(misspelling2, misspellingMap.get("madrid"));
    }

    @Test
    public void testBuildMisspellingAutomaton() {
        Misspelling misspelling1 =
                Misspelling.builder().setWord("aun").setComment("aún").setCaseSensitive(false).build();
        Misspelling misspelling2 =
                Misspelling.builder().setWord("madrid").setComment("Madrid").setCaseSensitive(true).build();
        List<Misspelling> misspellingList = Arrays.asList(misspelling1, misspelling2);

        RunAutomaton automaton = MisspellingManager.buildMisspellingAutomaton(misspellingList);

        // This automaton finds the sequence of letters even if not complete words
        String text = "En madrid Aun pauna.";
        List<ArticleReplacement> replacements = ArticleReplacementFinder.findReplacements(text, automaton, ReplacementType.MISSPELLING);
        Assert.assertEquals(3, replacements.size());
        Assert.assertEquals("madrid", replacements.get(0).getText());
        Assert.assertEquals("Aun", replacements.get(1).getText());
        Assert.assertEquals("aun", replacements.get(2).getText());
    }

    @Test
    public void testBuildUppercaseAutomaton() {
        Misspelling misspelling1 =
                Misspelling.builder().setWord("Marzo").setComment("marzo").setCaseSensitive(true).build();
        Misspelling misspelling2 =
                Misspelling.builder().setWord("Febrero").setComment("febrero").setCaseSensitive(true).build();
        List<Misspelling> misspellingList = Arrays.asList(misspelling1, misspelling2);

        RunAutomaton automaton = MisspellingManager.buildUppercaseAutomaton(misspellingList);

        // This automaton finds the sequence of letters even if not complete words
        String text = "En Febrero. Marzo.";
        List<ArticleReplacement> replacements = ArticleReplacementFinder.findReplacements(text, automaton, ReplacementType.MISSPELLING);
        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(". Marzo", replacements.get(0).getText());
    }

    @Test
    public void testFindMisspellingByWord() {
        Map<String, Misspelling> misspellings = new HashMap<>(1);
        Misspelling misspelling =
                Misspelling.builder().setWord("madrid").setComment("Madrid").setCaseSensitive(true).build();
        misspellings.put("madrid", misspelling);
        misspellingManager.setMisspellings(misspellings);

        Assert.assertEquals(misspelling, misspellingManager.findMisspellingByWord("madrid"));
        Assert.assertNull(misspellingManager.findMisspellingByWord("Madrid"));
    }

    @Test
    @Ignore
    public void testFindMisspellingsPerformance() throws WikipediaException {
        LOGGER.info("BEGIN FIND POTENTIAL ERRORS EXPERIMENT");

        String text = "";
        String misspellingText = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(MisspellingManagerTest.class.getResource("/article-longest.txt").toURI())),
                    StandardCharsets.UTF_8);
            misspellingText = new String(Files.readAllBytes(Paths.get(MisspellingManagerTest.class.getResource("/misspelling-list.txt").toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("", e);
        }
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(misspellingText);
        // Pre-load the misspellings map
        LOGGER.info("Loading misspellings...");
        misspellingManager.updateMisspellings();

        // Test 1 : Find all the words and check if they are potential errors
        LOGGER.info("\nBuilding automaton...");
        RunAutomaton automatonWord = new RunAutomaton(new RegExp("(<L>|<N>)+").toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("BEGIN TEST #1");
        long start1 = System.currentTimeMillis();
        List<ArticleReplacement> textWords = ArticleReplacementFinder.findReplacements(text, automatonWord, ReplacementType.IGNORED);
        // For each word, check if it is a known potential misspelling.
        int count1 = 0;
        for (ArticleReplacement textWord : textWords) {
            String originalText = textWord.getText();
            Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
            if (wordMisspelling != null) {
                LOGGER.info("MATCH: {}", wordMisspelling.getWord());
                count1++;
            }
        }
        long timeElapsed1 = System.currentTimeMillis() - start1;
        LOGGER.info("TEST 1: {} ms / {} results", +timeElapsed1, count1);
        LOGGER.info("Words: {}\n", textWords.size());


        // Test 2 : Build a long long regex with all the potential errors and match the text
        LOGGER.info("Building alternations...");
        Set<Misspelling> misspellingList = new HashSet<>(misspellingManager.getMisspellings().values());
        Collection<String> alternations = new ArrayList<>(misspellingList.size());
        for (Misspelling misspelling : misspellingList) {
            if (misspelling.isCaseSensitive()) {
                alternations.add(misspelling.getWord());
            } else {
                String word = misspelling.getWord();
                String firstLetter = word.substring(0, 1);
                String newWord = '[' + firstLetter + firstLetter.toUpperCase(Locale.forLanguageTag("es")) + ']' + word.substring(1);
                alternations.add(newWord);
            }
        }

        LOGGER.info("Building automaton...");
        String regexAlternations2 = " (" + StringUtils.join(alternations, "|") + ") ";
        // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
        RunAutomaton automatonMisspellings2 = new RunAutomaton(new RegExp(regexAlternations2).toAutomaton());

        LOGGER.info("BEGIN TEST #2");
        long start2 = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        Pattern nonAlphanumeric = Pattern.compile("[^\\p{L}\\p{N}]");
        String cleanText2 = nonAlphanumeric.matcher(text).replaceAll(" ");

        AutomatonMatcher automatonMatcher2 = automatonMisspellings2.newMatcher(cleanText2);
        int count2 = 0;
        while (automatonMatcher2.find()) {
            String originalText = automatonMatcher2.group(0).trim();
            Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
            if (wordMisspelling != null) {
                LOGGER.info("MATCH: {}", wordMisspelling.getWord());
                count2++;
            }
        }
        long timeElapsed2 = System.currentTimeMillis() - start2;
        LOGGER.info("TEST 2: {} ms / {} results", +timeElapsed2, count2);
        LOGGER.info("Misspellings: {}\n", misspellingList.size());


        // Test 2B : Change the way we build the long regex with the alternations (no leading space)
        LOGGER.info("Building automaton...");
        String regexAlternations2B = '(' + StringUtils.join(alternations, "|") + ") ";
        // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
        RunAutomaton automatonMisspellings2B = new RunAutomaton(new RegExp(regexAlternations2B).toAutomaton());

        LOGGER.info("BEGIN TEST #2B");
        long start2B = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        String cleanText2B = nonAlphanumeric.matcher(text).replaceAll(" ");

        AutomatonMatcher automatonMatcher2B = automatonMisspellings2B.newMatcher(cleanText2B);
        int count2B = 0;
        while (automatonMatcher2B.find()) {
            // Check if the found word is complete, i. e. check the character before the match
            if (" ".equals(cleanText2B.substring(automatonMatcher2B.start() - 1, automatonMatcher2B.start()))) {
                String originalText = automatonMatcher2B.group(0).trim();
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
                if (wordMisspelling != null) {
                    LOGGER.info("MATCH: {}", wordMisspelling.getWord());
                    count2B++;
                }
            }
        }
        long timeElapsed2B = System.currentTimeMillis() - start2B;
        LOGGER.info("TEST 2B: {} ms / {} results\n", +timeElapsed2B, count2B);


        // Test 2C : Change the way we build the long regex with the alternations (no leading or trailing space)
        LOGGER.info("Building automaton...");
        String regexAlternations2C = '(' + StringUtils.join(alternations, "|") + ')';
        // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
        RunAutomaton automatonMisspellings2C = new RunAutomaton(new RegExp(regexAlternations2C).toAutomaton());

        LOGGER.info("BEGIN TEST #2C");
        long start2C = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        String cleanText2C = nonAlphanumeric.matcher(text).replaceAll(" ");

        AutomatonMatcher automatonMatcher2C = automatonMisspellings2C.newMatcher(cleanText2C);
        int count2C = 0;
        while (automatonMatcher2C.find()) {
            // Check if the found word is complete, i. e. check the character before the match
            if (" ".equals(cleanText2C.substring(automatonMatcher2C.start() - 1, automatonMatcher2C.start()))
                    && " ".equals(cleanText2C.substring(automatonMatcher2C.end(), automatonMatcher2C.end() + 1))) {
                String originalText = automatonMatcher2C.group(0).trim();
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
                if (wordMisspelling != null) {
                    LOGGER.info("MATCH: {}", wordMisspelling.getWord());
                    count2C++;
                }
            }
        }
        long timeElapsed2C = System.currentTimeMillis() - start2C;
        LOGGER.info("TEST 2C: {} ms / {} results\n", +timeElapsed2C, count2C);

/*
        // Test 3 : Create one regex for each misspelling and try to match the text
        // Heap Size Exception

        List<RunAutomaton> misspellingAutomatonList = new ArrayList<>(alternations.size());
        for (String alternation : alternations) {
            misspellingAutomatonList.add(new RunAutomaton(new RegExp(" " + alternation + " ").toAutomaton()));
        }

        LOGGER.info("BEGIN TEST #3");
        start = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        cleanText = text.replaceAll("[^\\p{L}\\p{N}]", " ");

        int count3 = 0;
        for (RunAutomaton misspellingAutomaton : misspellingAutomatonList) {
            automatonMatcher = misspellingAutomaton.newMatcher(cleanText);
            while (automatonMatcher.find()) {
                String originalText = automatonMatcher.group(0);
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
                if (wordMisspelling != null) {
                    LOGGER.info("MATCH: " + originalText);
                    count3++;
                }
            }
        }
        timeElapsed = System.currentTimeMillis() - start;
        LOGGER.info("TEST 3: " + timeElapsed + " ms / " + count3 + " results");
        LOGGER.info();
*/
    }

}
