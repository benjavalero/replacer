package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.persistence.ReplacementType;
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
        List<ArticleReplacement> matches = falsePositiveFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(8, matches.size());
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(3).setText("sólo").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(11).setText("Éstos").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(23).setText("Index").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(29).setText("Online").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(39).setText("ésta").build()));
        Assert.assertFalse(matches.contains(ArticleReplacement.builder().setStart(44).setText("Tropicos.org").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(57).setText("Aquél").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(63).setText("aquéllo").build()));
        Assert.assertTrue(matches.contains(ArticleReplacement.builder().setStart(71).setText("Saint-Martin").build()));
    }

    @Test
    public void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        List<ArticleReplacement> matches1 = falsePositiveFinder.findIgnoredReplacements(text1);

        Assert.assertFalse(matches1.isEmpty());
        Assert.assertEquals(1, matches1.size());
        Assert.assertTrue(matches1.contains(ArticleReplacement.builder().setStart(2).setText("Top Album").build()));
        // Only the first match is found
        Assert.assertFalse(matches1.contains(ArticleReplacement.builder().setStart(6).setText("Album Chart").build()));

        String text2 = "A Topp Album Chart.";
        List<ArticleReplacement> matches2 = falsePositiveFinder.findIgnoredReplacements(text2);

        Assert.assertFalse(matches2.isEmpty());
        Assert.assertEquals(1, matches2.size());
        Assert.assertTrue(matches2.contains(ArticleReplacement.builder().setStart(7).setText("Album Chart").build()));
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
        for (ArticleReplacement textWord : falsePositiveFinder.findReplacements(text, automaton, ReplacementType.IGNORED)) {
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
            for (ArticleReplacement textWord : falsePositiveFinder.findReplacements(text, automaton2, ReplacementType.IGNORED)) {
                LOGGER.info("MATCH: {}", textWord.getText());
                count2++;
            }
        }
        long timeElapsed2 = System.currentTimeMillis() - start2;
        LOGGER.info("TEST 2: {} ms / {} results\n", timeElapsed2, count2);
    }

}
