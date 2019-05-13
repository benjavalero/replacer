package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(SpringRunner.class)
public class FalsePositiveFinderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinderTest.class);

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private FalsePositiveFinder falsePositiveFinder;

    @Before
    public void setUp() throws IOException, URISyntaxException, WikipediaException {
        falsePositiveFinder = new FalsePositiveFinder();
        MockitoAnnotations.initMocks(this);

        String text = new String(Files.readAllBytes(Paths.get(
                FalsePositiveFinderTest.class.getResource("/false-positives.txt").toURI())), StandardCharsets.UTF_8);
        Mockito.when(wikipediaService.getFalsePositiveListPageContent()).thenReturn(text);

        falsePositiveFinder.updateFalsePositives();
    }

    @Test
    public void testLoadFalsePositives() throws WikipediaException {
        List<String> falsePositives = falsePositiveFinder.loadFalsePositives();
        Assert.assertFalse(falsePositives.isEmpty());
        Assert.assertTrue(falsePositives.contains("Index"));
        Assert.assertTrue(falsePositives.contains("Magazine"));
        Assert.assertFalse(falsePositives.contains("# LIST OF FALSE POSITIVES"));
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un sólo de Éstos en el Index Online de ésta Tropicos.org Aquél aquéllo Saint-Martin.";
        List<MatchResult> matches = falsePositiveFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(8, matches.size());
        Assert.assertTrue(matches.contains(new MatchResult(3, "sólo")));
        Assert.assertTrue(matches.contains(new MatchResult(11, "Éstos")));
        Assert.assertTrue(matches.contains(new MatchResult(23, "Index")));
        Assert.assertTrue(matches.contains(new MatchResult(29, "Online")));
        Assert.assertTrue(matches.contains(new MatchResult(39, "ésta")));
        Assert.assertFalse(matches.contains(new MatchResult(44, "Tropicos.org")));
        Assert.assertTrue(matches.contains(new MatchResult(57, "Aquél")));
        Assert.assertTrue(matches.contains(new MatchResult(63, "aquéllo")));
        Assert.assertTrue(matches.contains(new MatchResult(71, "Saint-Martin")));
    }

    @Test
    public void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        List<MatchResult> matches1 = falsePositiveFinder.findIgnoredReplacements(text1);

        Assert.assertFalse(matches1.isEmpty());
        Assert.assertEquals(1, matches1.size());
        Assert.assertTrue(matches1.contains(new MatchResult(2, "Top Album")));
        // Only the first match is found
        Assert.assertFalse(matches1.contains(new MatchResult(6, "Album Chart")));

        String text2 = "A Topp Album Chart.";
        List<MatchResult> matches2 = falsePositiveFinder.findIgnoredReplacements(text2);

        Assert.assertFalse(matches2.isEmpty());
        Assert.assertEquals(1, matches2.size());
        Assert.assertTrue(matches2.contains(new MatchResult(7, "Album Chart")));
    }

    @Test
    @Ignore
    public void testFalsePositivesAutomatons() throws WikipediaException {
        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(FalsePositiveFinderTest.class.getResource("/article-longest.txt").toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("", e);
        }

        List<String> falsePositives = falsePositiveFinder.loadFalsePositives();

        // Test 1 : One single automaton with all the alternations
        String alternations = StringUtils.collectionToDelimitedString(falsePositives, "|");
        RegExp r = new RegExp(alternations);
        RunAutomaton automaton = new RunAutomaton(r.toAutomaton());

        LOGGER.info("BEGIN TEST #1");
        long start1 = System.currentTimeMillis();
        int count1 = 0;
        for (MatchResult textWord : falsePositiveFinder.findMatchResults(text, automaton)) {
            LOGGER.info("MATCH: {}", textWord.getText());
            count1++;
        }
        long timeElapsed1 = System.currentTimeMillis() - start1;
        LOGGER.info("TEST 1: {} ms / {} results\n", timeElapsed1, count1);

        // Test 2 : One automaton per false positive line
        Collection<RunAutomaton> automatons = new ArrayList<>(falsePositives.size());
        for (String falsePositive : falsePositives) {
            RegExp r2 = new RegExp(falsePositive);
            automatons.add(new RunAutomaton(r2.toAutomaton()));
        }

        LOGGER.info("BEGIN TEST #2");
        long start2 = System.currentTimeMillis();
        int count2 = 0;
        for (RunAutomaton automaton2 : automatons) {
            for (MatchResult textWord : falsePositiveFinder.findMatchResults(text, automaton2)) {
                LOGGER.info("MATCH: {}", textWord.getText());
                count2++;
            }
        }
        long timeElapsed2 = System.currentTimeMillis() - start2;
        LOGGER.info("TEST 2: {} ms / {} results\n", timeElapsed2, count2);
    }

}
