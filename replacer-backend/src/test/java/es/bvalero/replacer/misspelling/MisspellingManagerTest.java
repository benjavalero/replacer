package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class MisspellingManagerTest {

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private MisspellingManager misspellingManager;

    @Before
    public void setUp() {
        misspellingManager = new MisspellingManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseMisspellingListText() {
        String misspellingListText = "Texto\n\n" +
                "A||B\n" + // No starting whitespace
                " C|cs|D\n" +
                " E|CS|F\n" +
                " G|H\n" + // Bad formatted
                " I||J\n" +
                " I||J\n" + // Duplicated
                " k||k\n" +
                " k||M\n"; // Duplicated but different comment

        Collection<Misspelling> misspellings = misspellingManager.parseMisspellingListText(misspellingListText);
        Assert.assertEquals(4, misspellings.size());
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("C").setCaseSensitive(true).setComment("D").build()));
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("E").setCaseSensitive(true).setComment("F").build()));
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("I").setCaseSensitive(false).setComment("J").build()));
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("k").setCaseSensitive(false).setComment("k").build()));
    }

    @Test
    public void testParseValidMisspellingWords() {
        String misspellingListText = " aguila||águila\n" +
                " m2||m²\n" + // Not valid with numbers
                " Castilla-León||Castilla y León\n" + // Valid with dashes
                " CD's||CD\n" + // Valid with single quotes
                " cm.||cm\n"; // Not valid with dots

        Collection<Misspelling> misspellings = misspellingManager.parseMisspellingListText(misspellingListText);
        Assert.assertEquals(3, misspellings.size());
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("aguila").setComment("águila").build()));
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("Castilla-León").setComment("Castilla y León").build()));
        Assert.assertTrue(misspellings.contains(
                Misspelling.builder().setWord("CD's").setComment("CD").build()));
    }

    @Test
    public void testDeleteObsoleteMisspellings() {
        Misspelling misspelling1 = Misspelling.builder().setWord("A").build();
        Misspelling misspelling2 = Misspelling.builder().setWord("B").build();
        misspellingManager.setMisspellings(new HashSet<>(Arrays.asList(misspelling1, misspelling2)));

        Mockito.verify(articleService, Mockito.times(0)).deleteReplacementsByTextIn(Mockito.anySet());

        Misspelling misspelling3 = Misspelling.builder().setWord("C").build();
        misspellingManager.setMisspellings(new HashSet<>(Arrays.asList(misspelling2, misspelling3)));

        Mockito.verify(articleService, Mockito.times(1)).deleteReplacementsByTextIn(Collections.singleton("A"));
    }

}
