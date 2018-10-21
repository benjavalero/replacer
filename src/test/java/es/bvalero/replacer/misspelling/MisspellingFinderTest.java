package es.bvalero.replacer.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.persistence.ReplacementType;
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
    public void testFindMisspellings() {
        String articleContent = "UM vonito Exemplo exemplo luma.";

        Misspelling misspelling1 = Misspelling.builder()
                .setWord("um")
                .build();
        Mockito.when(misspellingManager.findMisspellingByWord("un")).thenReturn(misspelling1);

        Misspelling misspelling2 = Misspelling.builder()
                .setWord("vonito")
                .setComment("bonito")
                .build();
        Mockito.when(misspellingManager.findMisspellingByWord("vonito")).thenReturn(misspelling2);

        Misspelling misspelling3 = Misspelling.builder()
                .setWord("exemplo")
                .setComment("ejemplo")
                .build();
        Mockito.when(misspellingManager.findMisspellingByWord("Exemplo")).thenReturn(misspelling3);
        Mockito.when(misspellingManager.findMisspellingByWord("exemplo")).thenReturn(misspelling3);

        String misspellingRegex = "([Uu]m|[Vv]onito|[Ee]xemplo)";
        RunAutomaton misspellingAutomaton = new RunAutomaton(new RegExp(misspellingRegex).toAutomaton());
        Mockito.when(misspellingManager.getMisspellingAutomaton()).thenReturn(misspellingAutomaton);

        List<ArticleReplacement> result = misspellingFinder.findReplacements(articleContent);

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());

        // "UN" will be ignored because it is all in uppercase and has not a known uppercase misspelling

        ArticleReplacement result1 = result.get(0);
        Assert.assertEquals("vonito", result1.getText());
        Assert.assertEquals(3, result1.getStart());
        Assert.assertEquals(ReplacementType.MISSPELLING, result1.getType());
        Assert.assertEquals("vonito", result1.getSubtype());

        ArticleReplacement result2 = result.get(1);
        Assert.assertEquals("Exemplo", result2.getText());
        Assert.assertEquals(10, result2.getStart());
        Assert.assertEquals(ReplacementType.MISSPELLING, result2.getType());
        Assert.assertEquals("exemplo", result2.getSubtype());

        ArticleReplacement result3 = result.get(2);
        Assert.assertEquals("exemplo", result3.getText());
        Assert.assertEquals(18, result3.getStart());
        Assert.assertEquals(ReplacementType.MISSPELLING, result3.getType());
        Assert.assertEquals("exemplo", result3.getSubtype());
    }

    @Test
    public void testFindMisspellingSuggestion() {
        Misspelling misspellingCS = Misspelling.builder().setWord("españa").setCaseSensitive(true).setComment("España").build();
        Misspelling misspellingCS2 = Misspelling.builder().setWord("Domingo").setCaseSensitive(true).setComment("domingo").build();
        Misspelling misspellingCI = Misspelling.builder().setWord("habia").setCaseSensitive(false).setComment("había").build();

        // Uppercase word + Case-sensitive
        Assert.assertEquals("domingo", MisspellingFinder
                .findMisspellingSuggestion("Domingo", misspellingCS2));

        // Uppercase word + Case-insensitive
        Assert.assertEquals("Había", MisspellingFinder
                .findMisspellingSuggestion("Habia", misspellingCI));

        // Lowercase word + Case-sensitive
        Assert.assertEquals("España", MisspellingFinder
                .findMisspellingSuggestion("españa", misspellingCS));

        // Lowercase word + Case-insensitive
        Assert.assertEquals("había", MisspellingFinder
                .findMisspellingSuggestion("habia", misspellingCI));
    }

    @Test
    public void testParseCommentSuggestions() {
        Misspelling misspelling1 = Misspelling.builder()
                .setWord("renuncio").setComment("renunció (3.ª persona), renuncio (1.ª persona)").build();
        List<String> suggestions1 = MisspellingFinder.parseCommentSuggestions(misspelling1);
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("renunció", suggestions1.get(0));

        Misspelling misspelling2 = Misspelling.builder()
                .setWord("remake").setComment("(nueva) versión o adaptación").build();
        List<String> suggestions2 = MisspellingFinder.parseCommentSuggestions(misspelling2);
        Assert.assertEquals(1, suggestions2.size());
        Assert.assertEquals("versión o adaptación", suggestions2.get(0));

        Misspelling misspelling3 = Misspelling.builder().setWord("desempeño")
                .setComment("desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)").build();
        List<String> suggestions3 = MisspellingFinder.parseCommentSuggestions(misspelling3);
        Assert.assertEquals(1, suggestions3.size());
        Assert.assertEquals("desempeñó", suggestions3.get(0));

        Misspelling misspelling4 = Misspelling.builder().setWord("k")
                .setComment("k (letra), que, qué, kg (kilogramo)").build();
        List<String> suggestions4 = MisspellingFinder.parseCommentSuggestions(misspelling4);
        Assert.assertEquals(3, suggestions4.size());
    }

}
