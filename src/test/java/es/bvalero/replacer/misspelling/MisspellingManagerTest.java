package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MisspellingManagerTest {

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
    public void testFindWikipediaMisspellingsWithErrors() throws Exception {
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenThrow(new WikipediaException());

        misspellingManager.updateMisspellings();
    }

    @Test
    public void testUpdateMisspellings() throws WikipediaException {
        String misspellingListText = "Texto\n" +
                "\n" +
                "A||B\n" +
                " C|cs|D\n" +
                " E|CS|F\n" + // No case sensitive
                " G|H\n" +
                " I||J\n" +
                " k||k (letra), que, qué, kg (kilogramo)\n" +
                " I||J\n" + // Duplicated
                " renuncio||renunció (3.ª persona), renuncio (1.ª persona)\n" +
                " remake||(nueva) versión o adaptación\n" +
                " desempeño||desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)";

        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(misspellingListText);

        misspellingManager.updateMisspellings();

        Assert.assertNull(misspellingManager.findMisspellingByWord("A"));

        Misspelling misspellingC = misspellingManager.findMisspellingByWord("C");
        Assert.assertNotNull(misspellingC);
        Assert.assertEquals("C", misspellingC.getWord());
        Assert.assertTrue(misspellingC.isCaseSensitive());
        Assert.assertEquals("D", misspellingC.getComment());

        Misspelling misspellingE = misspellingManager.findMisspellingByWord("E");
        Assert.assertNotNull(misspellingE);
        Assert.assertEquals("E", misspellingE.getWord());
        Assert.assertTrue(misspellingE.isCaseSensitive());
        Assert.assertEquals("F", misspellingE.getComment());

        Assert.assertNull(misspellingManager.findMisspellingByWord("G"));

        Misspelling misspellingI = misspellingManager.findMisspellingByWord("I");
        Assert.assertNotNull(misspellingI);
        Assert.assertEquals("i", misspellingI.getWord());
        Assert.assertFalse(misspellingI.isCaseSensitive());
        Assert.assertEquals("J", misspellingI.getComment());

        Misspelling misspellingK = misspellingManager.findMisspellingByWord("K");
        Assert.assertNotNull(misspellingK);
        Assert.assertEquals("k", misspellingK.getWord());
        Assert.assertEquals(3, misspellingK.getSuggestions().size());
        Assert.assertTrue(misspellingK.getSuggestions().contains("qué"));
        Assert.assertFalse(misspellingK.getSuggestions().contains("k"));

        Misspelling misspellingRenuncio = misspellingManager.findMisspellingByWord("renuncio");
        Assert.assertNotNull(misspellingRenuncio);
        Assert.assertEquals(1, misspellingRenuncio.getSuggestions().size());
        Assert.assertEquals("renunció", misspellingRenuncio.getSuggestions().get(0));

        Misspelling misspellingRemake = misspellingManager.findMisspellingByWord("remake");
        Assert.assertNotNull(misspellingRemake);
        Assert.assertFalse(misspellingRemake.getSuggestions().isEmpty());
        Assert.assertEquals("versión o adaptación", misspellingRemake.getSuggestions().get(0));

        // Test with commas between brackets
        Misspelling misspellingDesempeno = misspellingManager.findMisspellingByWord("desempeño");
        Assert.assertNotNull(misspellingDesempeno);
        Assert.assertEquals(1, misspellingDesempeno.getSuggestions().size());
        Assert.assertEquals("desempeñó", misspellingDesempeno.getSuggestions().get(0));
    }

    @Test
    public void testFindMisspellingByWord() throws WikipediaException {
        String wikiText = " conprar||comprar\n madrid|cs|Madrid\n álvaro|cs|Álvaro";
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(wikiText);

        Assert.assertEquals("conprar",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("conprar")).getWord());
        Assert.assertEquals("conprar",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("Conprar")).getWord());
        Assert.assertEquals("madrid",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("madrid")).getWord());
        Assert.assertEquals("álvaro",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("álvaro")).getWord());
        Assert.assertNull(misspellingManager.findMisspellingByWord("Madrid"));
        Assert.assertNull(misspellingManager.findMisspellingByWord("Álvaro"));
    }

    @Test
    @Ignore
    public void testFindMisspellingsPerformance() throws WikipediaException {
        System.out.println("BEGIN FIND POTENTIAL ERRORS EXPERIMENT");

        String text = null;
        String misspellingText = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(MisspellingManagerTest.class.getResource("/article-longest.txt").toURI())),
                    StandardCharsets.UTF_8);
            misspellingText = new String(Files.readAllBytes(Paths.get(MisspellingManagerTest.class.getResource("/misspelling-list.txt").toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(misspellingText);
        // Pre-load the misspellings map
        System.out.println("Loading misspellings...");
        misspellingManager.updateMisspellings();
        System.out.println();

        // Test 1 : Find all the words and check if they are potential errors
        System.out.println("Building automaton...");
        RunAutomaton automatonWord = new RunAutomaton(new RegExp("(<L>|<N>)+").toAutomaton(new DatatypesAutomatonProvider()));

        System.out.println("BEGIN TEST #1");
        long start = System.currentTimeMillis();
        List<RegexMatch> textWords = RegExUtils.findMatchesAutomaton(text, automatonWord);
        // For each word, check if it is a known potential misspelling.
        int count1 = 0;
        for (RegexMatch textWord : textWords) {
            String originalText = textWord.getOriginalText();
            Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
            if (wordMisspelling != null) {
                System.out.println("MATCH: " + originalText);
                count1++;
            }
        }
        long timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 1: " + timeElapsed + " ms / " + count1 + " results");
        System.out.println("Words: " + textWords.size());
        System.out.println();


        // Test 2 : Build a long long regex with all the potential errors and match the text
        System.out.println("Building alternations...");
        Set<Misspelling> misspellingList = new HashSet<>(misspellingManager.getMisspellingMap().values());
        List<String> alternations = new ArrayList<>(misspellingList.size());
        for (Misspelling misspelling : misspellingList) {
            if (misspelling.isCaseSensitive()) {
                alternations.add(misspelling.getWord());
            } else {
                String word = misspelling.getWord();
                String firstLetter = word.substring(0, 1);
                String newWord = "[" + firstLetter + firstLetter.toUpperCase(Locale.forLanguageTag("es")) + "]" + word.substring(1);
                alternations.add(newWord);
            }
        }

        System.out.println("Building automaton...");
        String regexAlternations = " (" + org.apache.commons.lang3.StringUtils.join(alternations, "|") + ") ";
        // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
        RunAutomaton automatonMisspellings = new RunAutomaton(new RegExp(regexAlternations).toAutomaton());

        System.out.println("BEGIN TEST #2");
        start = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        String cleanText = text.replaceAll("[^\\p{L}\\p{N}]", " ");

        AutomatonMatcher automatonMatcher = automatonMisspellings.newMatcher(cleanText);
        int count2 = 0;
        while (automatonMatcher.find()) {
            String originalText = automatonMatcher.group(0).trim();
            Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
            if (wordMisspelling != null) {
                System.out.println("MATCH: " + originalText);
                count2++;
            }
        }
        timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 2: " + timeElapsed + " ms / " + count2 + " results");
        System.out.println("Misspellings: " + misspellingList.size());
        System.out.println();


        // Test 2B : Change the way we build the long regex with the alternations (no leading space)
        System.out.println("Building automaton...");
        regexAlternations = "(" + org.apache.commons.lang3.StringUtils.join(alternations, "|") + ") ";
        // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
        automatonMisspellings = new RunAutomaton(new RegExp(regexAlternations).toAutomaton());

        System.out.println("BEGIN TEST #2B");
        start = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        cleanText = text.replaceAll("[^\\p{L}\\p{N}]", " ");

        automatonMatcher = automatonMisspellings.newMatcher(cleanText);
        int count2B = 0;
        while (automatonMatcher.find()) {
            // Check if the found word is complete, i. e. check the character before the match
            if (cleanText.substring(automatonMatcher.start() - 1, automatonMatcher.start()).equals(" ")) {
                String originalText = automatonMatcher.group(0).trim();
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
                if (wordMisspelling != null) {
                    System.out.println("MATCH: " + originalText);
                    count2B++;
                }
            }
        }
        timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 2B: " + timeElapsed + " ms / " + count2B + " results");
        System.out.println();


        // Test 2C : Change the way we build the long regex with the alternations (no leading or trailing space)
        System.out.println("Building automaton...");
        regexAlternations = "(" + org.apache.commons.lang3.StringUtils.join(alternations, "|") + ")";
        // IMPORTANT : WE NEED AT LEAST 2 MB OF STACK SIZE -Xss2m !!!
        automatonMisspellings = new RunAutomaton(new RegExp(regexAlternations).toAutomaton());

        System.out.println("BEGIN TEST #2C");
        start = System.currentTimeMillis();

        // Replace any character non-alphanumeric with a whitespace
        cleanText = text.replaceAll("[^\\p{L}\\p{N}]", " ");

        automatonMatcher = automatonMisspellings.newMatcher(cleanText);
        int count2C = 0;
        while (automatonMatcher.find()) {
            // Check if the found word is complete, i. e. check the character before the match
            if (cleanText.substring(automatonMatcher.start() - 1, automatonMatcher.start()).equals(" ")
                    && cleanText.substring(automatonMatcher.end(), automatonMatcher.end() + 1).equals(" ")) {
                String originalText = automatonMatcher.group(0).trim();
                Misspelling wordMisspelling = misspellingManager.findMisspellingByWord(originalText);
                if (wordMisspelling != null) {
                    System.out.println("MATCH: " + originalText);
                    count2C++;
                }
            }
        }
        timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 2C: " + timeElapsed + " ms / " + count2C + " results");
        System.out.println();

/*
        // Test 3 : Create one regex for each misspelling and try to match the text
        // Heap Size Exception

        List<RunAutomaton> misspellingAutomatonList = new ArrayList<>(alternations.size());
        for (String alternation : alternations) {
            misspellingAutomatonList.add(new RunAutomaton(new RegExp(" " + alternation + " ").toAutomaton()));
        }

        System.out.println("BEGIN TEST #3");
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
                    System.out.println("MATCH: " + originalText);
                    count3++;
                }
            }
        }
        timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 3: " + timeElapsed + " ms / " + count3 + " results");
        System.out.println();
*/
    }

}
