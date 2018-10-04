package es.bvalero.replacer.article.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.persistence.ReplacementType;
import es.bvalero.replacer.misspelling.Misspelling;
import es.bvalero.replacer.misspelling.MisspellingManager;
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
        String articleContent = "UM vonito Exemplo exemplo.";

        Misspelling misspelling1 = new Misspelling("um", false, "un");
        Mockito.when(misspellingManager.findMisspellingByWord("un")).thenReturn(misspelling1);

        Misspelling misspelling2 = new Misspelling("vonito", false, "bonito");
        Mockito.when(misspellingManager.findMisspellingByWord("vonito")).thenReturn(misspelling2);

        Misspelling misspelling3 = new Misspelling("exemplo", false, "ejemplo");
        Mockito.when(misspellingManager.findMisspellingByWord("Exemplo")).thenReturn(misspelling3);
        Mockito.when(misspellingManager.findMisspellingByWord("exemplo")).thenReturn(misspelling3);

        String misspellingRegex = "([Uu]n|[Vv]onito|[Ee]xemplo)";
        RunAutomaton misspellingAutomaton = new RunAutomaton(new RegExp(misspellingRegex).toAutomaton());
        Mockito.when(misspellingManager.getMisspellingAlternationsAutomaton()).thenReturn(misspellingAutomaton);

        List<ArticleReplacement> result = misspellingFinder.findPotentialErrors(articleContent);

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());

        // "UN" will be ignored because it is all in uppercase and has not a known uppercase misspelling
        // Results are inverse order

        ArticleReplacement result1 = result.get(0);
        Assert.assertEquals("vonito", result1.getOriginalText());
        Assert.assertEquals(3, result1.getPosition());
        Assert.assertEquals(ReplacementType.MISSPELLING, result1.getType());
        Assert.assertEquals("vonito", result1.getSubtype());
        Assert.assertEquals("bonito", result1.getProposedFixes().get(0));

        ArticleReplacement result2 = result.get(1);
        Assert.assertEquals("Exemplo", result2.getOriginalText());
        Assert.assertEquals(10, result2.getPosition());
        Assert.assertEquals(ReplacementType.MISSPELLING, result2.getType());
        Assert.assertEquals("exemplo", result2.getSubtype());
        Assert.assertEquals("Ejemplo", result2.getProposedFixes().get(0));

        ArticleReplacement result3 = result.get(2);
        Assert.assertEquals("exemplo", result3.getOriginalText());
        Assert.assertEquals(18, result3.getPosition());
        Assert.assertEquals(ReplacementType.MISSPELLING, result3.getType());
        Assert.assertEquals("exemplo", result3.getSubtype());
        Assert.assertEquals("ejemplo", result3.getProposedFixes().get(0));
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
