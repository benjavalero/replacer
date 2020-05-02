package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MisspellingSimpleFinderTest {
    private static final SetValuedMap<WikipediaLanguage, Misspelling> EMPTY_MAP = new HashSetValuedHashMap<>();

    private MisspellingSimpleFinder misspellingFinder;

    @BeforeEach
    public void setUp() {
        misspellingFinder = new MisspellingSimpleFinder();
    }

    @Test
    public void testFindMisspellingsNoResults() {
        String text = "Sample text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "b");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    public void testFindMisspellingsWithResults() {
        String text = "sample text.";
        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("sample", "ejemplo");
        Misspelling misspelling2 = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(2, results.size());

        Replacement result1 = results.get(0);
        Assertions.assertEquals("sample", result1.getText());
        Assertions.assertEquals(0, result1.getStart());
        Assertions.assertEquals("sample", result1.getSubtype());

        Replacement result2 = results.get(1);
        Assertions.assertEquals("text", result2.getText());
        Assertions.assertEquals(7, result2.getStart());
        Assertions.assertEquals("text", result2.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithResultCaseInsensitive() {
        String text = "Sample Text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("Text", result.getText());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithResultCaseSensitive() {
        String text = "text Text";
        Misspelling misspelling = Misspelling.of("text", true, "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("text", result.getText());
        Assertions.assertEquals(0, result.getStart());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithCompleteWord() {
        String text = "Texto Text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("Text", result.getText());
        Assertions.assertEquals(6, result.getStart());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsWithUppercase() {
        String text = "SAMPLE TEXT";
        Misspelling misspelling1 = Misspelling.of("SAMPLE", true, "sample");
        Misspelling misspelling2 = Misspelling.of("text", false, "texto");
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("SAMPLE", result.getText());
        Assertions.assertEquals(0, result.getStart());
        Assertions.assertEquals("SAMPLE", result.getSubtype());
    }

    @Test
    public void testFindMisspellingsBetweenUnderscores() {
        String text = "A _Text Text_ _Text_ Text.";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("Text", result.getText());
        Assertions.assertEquals(21, result.getStart());
        Assertions.assertEquals("text", result.getSubtype());
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
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertEquals("España", results.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals("domingo", results.get(1).getSuggestions().get(0).getText());
        Assertions.assertEquals("águila", results.get(2).getSuggestions().get(0).getText());
        Assertions.assertEquals("Águila", results.get(3).getSuggestions().get(0).getText());
    }

    @Test
    public void testFindMisspellingSuggestionSameWordFirst() {
        String word = "entreno";
        String comment = "entrenó (verbo), entreno (sustantivo)";
        Misspelling misspelling = Misspelling.ofCaseInsensitive(word, comment);
        String text = String.format("Un %s.", word);

        // Fake the update of the misspelling list in the misspelling manager
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(3, results.get(0).getStart());
        Assertions.assertEquals(word, results.get(0).getText());
        Assertions.assertEquals(2, results.get(0).getSuggestions().size());
        Assertions.assertEquals(word, results.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testMisspellingListEmpty() {
        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, EMPTY_MAP));

        Assertions.assertTrue(misspellingFinder.findList("A sample text", WikipediaLanguage.SPANISH).isEmpty());
    }
}
