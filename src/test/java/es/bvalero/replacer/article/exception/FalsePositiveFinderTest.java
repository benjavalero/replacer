package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.misspelling.MisspellingManagerTest;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FalsePositiveFinderTest {

    @Test
    public void testLoadFalsePositives() {
        List<String> falsePositives = FalsePositiveFinder.loadFalsePositives();
        Assert.assertFalse(falsePositives.isEmpty());
        Assert.assertTrue(falsePositives.contains("Index"));
        Assert.assertTrue(falsePositives.contains("Magazine"));
        Assert.assertFalse(falsePositives.contains("# LIST OF FALSE POSITIVES"));
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un sólo de Éstos en el Index Online de ésta Tropicos.org Aquél aquéllo Saint-Martin.";

        FalsePositiveFinder falsePositiveFinder = new FalsePositiveFinder();
        List<RegexMatch> matches = falsePositiveFinder.findExceptionMatches(text, false);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(8, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(3, "sólo")));
        Assert.assertTrue(matches.contains(new RegexMatch(11, "Éstos")));
        Assert.assertTrue(matches.contains(new RegexMatch(23, "Index")));
        Assert.assertTrue(matches.contains(new RegexMatch(29, "Online")));
        Assert.assertTrue(matches.contains(new RegexMatch(39, "ésta")));
        Assert.assertFalse(matches.contains(new RegexMatch(44, "Tropicos.org")));
        Assert.assertTrue(matches.contains(new RegexMatch(57, "Aquél")));
        Assert.assertTrue(matches.contains(new RegexMatch(63, "aquéllo")));
        Assert.assertTrue(matches.contains(new RegexMatch(71, "Saint-Martin")));
    }

    public void testFalsePositivesAutomatons() throws InterruptedException {
        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(MisspellingManagerTest.class.getResource("/article-longest.txt").toURI())),
                    StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        List<String> falsePositives = FalsePositiveFinder.loadFalsePositives();

        // Test 1 : One single automaton with all the alternations
        String alternations = StringUtils.collectionToDelimitedString(falsePositives, "|");
        RegExp r = new RegExp(alternations);
        RunAutomaton automaton = new RunAutomaton(r.toAutomaton());

        System.out.println("BEGIN TEST #1");
        long start = System.currentTimeMillis();
        int count1 = 0;
        for (RegexMatch textWord : RegExUtils.findMatchesAutomaton(text, automaton)) {
            System.out.println("MATCH: " + textWord.getOriginalText());
            count1++;
        }
        long timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 1: " + timeElapsed + " ms / " + count1 + " results");
        System.out.println();

        System.out.println("Cleaning garbage...");
        System.gc();
        Thread.sleep(10000); // to allow GC do its job

        // Test 2 : One automaton per false positive line
        List<RunAutomaton> automatons = new ArrayList<>(falsePositives.size());
        for (String falsePositive : falsePositives) {
            RegExp r2 = new RegExp(falsePositive);
            automatons.add(new RunAutomaton(r2.toAutomaton()));
        }

        System.out.println("BEGIN TEST #2");
        start = System.currentTimeMillis();
        int count2 = 0;
        for (RunAutomaton automaton2 : automatons) {
            for (RegexMatch textWord : RegExUtils.findMatchesAutomaton(text, automaton2)) {
                System.out.println("MATCH: " + textWord.getOriginalText());
                count2++;
            }
        }
        timeElapsed = System.currentTimeMillis() - start;
        System.out.println("TEST 2: " + timeElapsed + " ms / " + count2 + " results");
        System.out.println();
    }

}
