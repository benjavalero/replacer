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
                new Misspelling("C", true, "D")));
        Assert.assertTrue(misspellings.contains(
                new Misspelling("E", true, "F")));
        Assert.assertTrue(misspellings.contains(
                new Misspelling("I", false, "J")));
        Assert.assertTrue(misspellings.contains(
                new Misspelling("k", false, "k")));
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
                new Misspelling("aguila", false, "águila")));
        Assert.assertTrue(misspellings.contains(
                new Misspelling("Castilla-León", false, "Castilla y León")));
        Assert.assertTrue(misspellings.contains(
                new Misspelling("CD's", false, "CD")));
    }

    @Test
    public void testDeleteObsoleteMisspellings() {
        Misspelling misspelling1 = new Misspelling("A", false, "B");
        Misspelling misspelling2 = new Misspelling("B", false, "C");
        misspellingManager.setMisspellings(new HashSet<>(Arrays.asList(misspelling1, misspelling2)));

        Mockito.verify(articleService, Mockito.times(0)).deleteReplacementsByTextIn(Mockito.anySet());

        Misspelling misspelling3 = new Misspelling("C", false, "D");
        misspellingManager.setMisspellings(new HashSet<>(Arrays.asList(misspelling2, misspelling3)));

        Mockito.verify(articleService, Mockito.times(1)).deleteReplacementsByTextIn(Collections.singleton("A"));
    }

}
