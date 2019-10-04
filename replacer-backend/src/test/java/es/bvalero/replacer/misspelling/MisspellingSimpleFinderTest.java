package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.Replacement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.util.*;

public class MisspellingSimpleFinderTest {

    private MisspellingSimpleFinder misspellingFinder;

    @Before
    public void setUp() {
        misspellingFinder = new MisspellingSimpleFinder();
    }

    @Test
    public void testFindMisspellingsNoResults() {
        String text = "Sample text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "b");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void testFindMisspellingsWithResults() {
        String text = "sample text.";
        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("sample", "ejemplo");
        Misspelling misspelling2 = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());

        Replacement result1 = results.get(0);
        Assert.assertEquals("sample", result1.getText());
        Assert.assertEquals(0, result1.getStart());
        Assert.assertEquals("sample", result1.getSubtype());

        Replacement result2 = results.get(1);
        Assert.assertEquals("text", result2.getText());
        Assert.assertEquals(7, result2.getStart());
        Assert.assertEquals("text", result2.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithResultCaseInsensitive() {
        String text = "Sample Text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assert.assertEquals("Text", result.getText());
        Assert.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithResultCaseSensitive() {
        String text = "text Text";
        Misspelling misspelling = Misspelling.of("text", true, "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assert.assertEquals("text", result.getText());
        Assert.assertEquals(0, result.getStart());
        Assert.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithCompleteWord() {
        String text = "Texto Text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assert.assertEquals("Text", result.getText());
        Assert.assertEquals(6, result.getStart());
        Assert.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithUppercase() {
        String text = "SAMPLE TEXT";
        Misspelling misspelling1 = Misspelling.of("SAMPLE", true, "sample");
        Misspelling misspelling2 = Misspelling.of("text", false, "texto");
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assert.assertEquals("SAMPLE", result.getText());
        Assert.assertEquals(0, result.getStart());
        Assert.assertEquals("SAMPLE", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsBetweenUnderscores() {
        String text = "A _Text Text_ _Text_ Text.";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assert.assertEquals("Text", result.getText());
        Assert.assertEquals(21, result.getStart());
        Assert.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingSuggestion() {
        // lowercase -> uppercase: españa -> España
        // uppercase -> lowercase: Domingo -> domingo
        // lowercase -> lowercase: aguila -> águila
        // uppercase -> uppercase: Aguila -> Águila
        String text = "españa Domingo aguila Aguila";
        Misspelling misspellingCS = Misspelling.of("españa", true, "España");
        Misspelling misspellingCS2 = Misspelling.of("Domingo", true, "domingo");
        Misspelling misspellingCI = Misspelling.of("aguila", false, "águila");

        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspellingCS, misspellingCS2, misspellingCI));

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertEquals("España", results.get(0).getSuggestions().get(0).getText());
        Assert.assertEquals("domingo", results.get(1).getSuggestions().get(0).getText());
        Assert.assertEquals("águila", results.get(2).getSuggestions().get(0).getText());
        Assert.assertEquals("Águila", results.get(3).getSuggestions().get(0).getText());
    }

    @Test
    public void testFindMisspellingSuggestionSameWordFirst() {
        String word = "entreno";
        String comment = "entrenó (verbo), entreno (sustantivo)";
        Misspelling misspelling = Misspelling.ofCaseInsensitive(word, comment);
        String text = String.format("Un %s.", word);

        // Fake the update of the misspelling list in the misspelling manager
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<Replacement> results = misspellingFinder.findReplacements(text);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(3, results.get(0).getStart());
        Assert.assertEquals(word, results.get(0).getText());
        Assert.assertEquals(2, results.get(0).getSuggestions().size());
        Assert.assertEquals(word, results.get(0).getSuggestions().get(0).getText());
    }

}
