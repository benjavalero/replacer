package es.bvalero.replacer.article.finder;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.misspelling.Misspelling;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.article.PotentialErrorType;
import es.bvalero.replacer.utils.StringUtils;
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

        ArticleReplacement result1 = result.get(0);
        Assert.assertEquals("vonito", result1.getOriginalText());
        Assert.assertEquals(3, result1.getPosition());
        Assert.assertEquals(PotentialErrorType.MISSPELLING, result1.getType());
        Assert.assertEquals("bonito", result1.getProposedFixes().get(0));

        ArticleReplacement result2 = result.get(1);
        Assert.assertEquals("Exemplo", result2.getOriginalText());
        Assert.assertEquals(10, result2.getPosition());
        Assert.assertEquals("Ejemplo", result2.getProposedFixes().get(0));
    }

    @Test
    public void testWordRegEx() {
        String word1 = "hola";
        String word2 = "km2";
        String word3 = "1km";
        String word4 = "España";
        String word5 = "Águila";
        String word6 = "cantó";
        String text = "#" + word1 + "-" + word2 + ". <!--" + word3 + "--> [[" + word4 + ", " + word5 + "]] :" + word6 + "|";

        MisspellingFinder misspellingFinder = new MisspellingFinder();

        List<RegexMatch> matches = misspellingFinder.findTextWords(text);
        Assert.assertEquals(5, matches.size());
        Assert.assertEquals(word1, matches.get(0).getOriginalText());
        Assert.assertEquals(word2, matches.get(1).getOriginalText());
        Assert.assertEquals(word4, matches.get(2).getOriginalText());
        Assert.assertEquals(word5, matches.get(3).getOriginalText());
        Assert.assertEquals(word6, matches.get(4).getOriginalText());

        // XML entities may appear when text is escaped
        matches = misspellingFinder.findTextWords(StringUtils.escapeText(text));

        Assert.assertEquals(7, matches.size());
        Assert.assertEquals(word1, matches.get(0).getOriginalText());
        Assert.assertEquals(word2, matches.get(1).getOriginalText());
        Assert.assertEquals("lt", matches.get(2).getOriginalText());
        Assert.assertEquals("gt", matches.get(3).getOriginalText());
        Assert.assertEquals(word4, matches.get(4).getOriginalText());
        Assert.assertEquals(word5, matches.get(5).getOriginalText());
        Assert.assertEquals(word6, matches.get(6).getOriginalText());
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
