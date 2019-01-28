package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.persistence.ReplacementType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.*;

public class MisspellingSlowerFinderTest {

    @InjectMocks
    private MisspellingSlowerFinder misspellingFinder;

    @Before
    public void setUp() {
        misspellingFinder = new MisspellingSlowerFinder();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindMisspellings() {
        String articleContent = "UM vonito Exemplo exemplo luma.";

        Misspelling misspelling1 = Misspelling.builder()
                .setWord("um")
                .build();

        Misspelling misspelling2 = Misspelling.builder()
                .setWord("vonito")
                .setComment("bonito")
                .build();

        Misspelling misspelling3 = Misspelling.builder()
                .setWord("exemplo")
                .setComment("ejemplo")
                .build();

        misspellingFinder.buildMisspellingRelatedFields(new HashSet<>(Arrays.asList(misspelling1, misspelling2, misspelling3)));

        List<ArticleReplacement> result = misspellingFinder.findReplacements(articleContent);

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());

        // "UM" will be ignored because it is all in uppercase and has not a known uppercase misspelling

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
        Assert.assertEquals("domingo", misspellingFinder
                .findMisspellingSuggestion("Domingo", misspellingCS2));

        // Uppercase word + Case-insensitive
        Assert.assertEquals("Había", misspellingFinder
                .findMisspellingSuggestion("Habia", misspellingCI));

        // Lowercase word + Case-sensitive
        Assert.assertEquals("España", misspellingFinder
                .findMisspellingSuggestion("españa", misspellingCS));

        // Lowercase word + Case-insensitive
        Assert.assertEquals("había", misspellingFinder
                .findMisspellingSuggestion("habia", misspellingCI));
    }

    @Test
    public void testParseCommentSuggestions() {
        Misspelling misspelling1 = Misspelling.builder()
                .setWord("renuncio").setComment("renunció (3.ª persona), renuncio (1.ª persona)").build();
        List<String> suggestions1 = misspellingFinder.parseCommentSuggestions(misspelling1);
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("renunció", suggestions1.get(0));

        Misspelling misspelling2 = Misspelling.builder()
                .setWord("remake").setComment("(nueva) versión o adaptación").build();
        List<String> suggestions2 = misspellingFinder.parseCommentSuggestions(misspelling2);
        Assert.assertEquals(1, suggestions2.size());
        Assert.assertEquals("versión o adaptación", suggestions2.get(0));

        Misspelling misspelling3 = Misspelling.builder().setWord("desempeño")
                .setComment("desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)").build();
        List<String> suggestions3 = misspellingFinder.parseCommentSuggestions(misspelling3);
        Assert.assertEquals(1, suggestions3.size());
        Assert.assertEquals("desempeñó", suggestions3.get(0));

        Misspelling misspelling4 = Misspelling.builder().setWord("k")
                .setComment("k (letra), que, qué, kg (kilogramo)").build();
        List<String> suggestions4 = misspellingFinder.parseCommentSuggestions(misspelling4);
        Assert.assertEquals(3, suggestions4.size());
    }

    @Test
    public void testSetFirstUpperCase() {
        Assert.assertEquals("Álvaro", misspellingFinder.setFirstUpperCase("Álvaro"));
        Assert.assertEquals("Úlcera", misspellingFinder.setFirstUpperCase("úlcera"));
        Assert.assertEquals("Ñ", misspellingFinder.setFirstUpperCase("ñ"));
    }

    @Test
    public void testBuildMisspellingMap() {
        Misspelling misspelling1 =
                Misspelling.builder().setWord("haver").setComment("haber").setCaseSensitive(false).build();
        Misspelling misspelling2 =
                Misspelling.builder().setWord("madrid").setComment("Madrid").setCaseSensitive(true).build();

        Set<Misspelling> misspellings = new HashSet<>(Arrays.asList(misspelling1, misspelling2));
        Map<String, Misspelling> misspellingMap = misspellingFinder.buildMisspellingMap(misspellings);

        Assert.assertEquals(3, misspellingMap.size());
        Assert.assertEquals(misspelling1, misspellingMap.get("haver"));
        Assert.assertEquals(misspelling1, misspellingMap.get("Haver"));
        Assert.assertEquals(misspelling2, misspellingMap.get("madrid"));
    }

    @Test
    public void testFindMisspellingByWord() {
        Misspelling misspelling = Misspelling.builder()
                .setWord("madrid").setComment("Madrid").setCaseSensitive(true).build();

        misspellingFinder.buildMisspellingRelatedFields(new HashSet<>(Collections.singletonList(misspelling)));

        Assert.assertEquals(misspelling, misspellingFinder.findMisspellingByWord("madrid"));
        Assert.assertNull(misspellingFinder.findMisspellingByWord("Madrid"));
    }

}
