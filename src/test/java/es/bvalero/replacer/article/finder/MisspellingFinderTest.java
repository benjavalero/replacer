package es.bvalero.replacer.article.finder;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.misspelling.Misspelling;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.RegexMatchType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class MisspellingFinderTest {

    @Mock
    private MisspellingManager misspellingManager;

    @InjectMocks
    private MisspellingFinder misspellingFinder;

    @Before
    public void setUp() {
        misspellingFinder = new MisspellingFinder();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindPotentialErrors() {
        String articleContent = "M2 vonito Exemplo";

        Misspelling misspelling1 = new Misspelling("m2", false, "m²");
        Mockito.when(misspellingManager.findMisspellingByWord("M2")).thenReturn(misspelling1);

        Misspelling misspelling2 = new Misspelling("vonito", false, "bonito");
        Mockito.when(misspellingManager.findMisspellingByWord("vonito")).thenReturn(misspelling2);

        Misspelling misspelling3 = new Misspelling("exemplo", false, "ejemplo");
        Mockito.when(misspellingManager.findMisspellingByWord("Exemplo")).thenReturn(misspelling3);

        List<ArticleReplacement> result = misspellingFinder.findPotentialErrors(articleContent);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        // "UN" will be ignored because it is all in uppercase and has a known misspelling
        // Results are inverse order

        ArticleReplacement result1 = result.get(1);
        Assert.assertEquals("vonito", result1.getOriginalText());
        Assert.assertEquals(3, result1.getPosition());
        Assert.assertEquals(RegexMatchType.MISSPELLING, result1.getType());
        Assert.assertEquals("bonito", result1.getProposedFixes().get(0));

        ArticleReplacement result2 = result.get(0);
        Assert.assertEquals("Exemplo", result2.getOriginalText());
        Assert.assertEquals(10, result2.getPosition());
        Assert.assertEquals("Ejemplo", result2.getProposedFixes().get(0));
    }

    @Test
    public void testWordRegEx() {
        String text = "#hola-adiós. <!-- Comentario --> [[España, Francia]]. Hola, adiós :Españísima|";

        MisspellingFinder misspellingFinder = new MisspellingFinder();
        List<RegexMatch> words = misspellingFinder.findTextWords(text);

        Assert.assertEquals(8, words.size());
        Assert.assertTrue(words.contains(new RegexMatch(1, "hola")));
        Assert.assertTrue(words.contains(new RegexMatch(6, "adiós")));
        Assert.assertTrue(words.contains(new RegexMatch(60, "adiós")));
        Assert.assertTrue(words.contains(new RegexMatch(18, "Comentario")));
        Assert.assertTrue(words.contains(new RegexMatch(35, "España")));
        Assert.assertTrue(words.contains(new RegexMatch(43, "Francia")));
        Assert.assertTrue(words.contains(new RegexMatch(54, "Hola")));
        Assert.assertTrue(words.contains(new RegexMatch(67, "Españísima")));
    }

    @Test
    public void testGetReplacementFromSuggestion() {
        Assert.assertEquals("España", misspellingFinder
                .getReplacementFromSuggestion("españa", "España", true));
        Assert.assertEquals("domingo", misspellingFinder
                .getReplacementFromSuggestion("Domingo", "domingo", true));
        Assert.assertEquals("había", misspellingFinder
                .getReplacementFromSuggestion("habia", "había", false));
        Assert.assertEquals("Había", misspellingFinder
                .getReplacementFromSuggestion("Habia", "había", false));
    }

}
