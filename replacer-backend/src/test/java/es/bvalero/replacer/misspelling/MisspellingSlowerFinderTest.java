package es.bvalero.replacer.misspelling;

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
