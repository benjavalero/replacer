package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MisspellingSimpleFinderTest {

    private static final SetValuedMap<WikipediaLanguage, Misspelling> EMPTY_MAP = new HashSetValuedHashMap<>();

    private MisspellingSimpleFinder misspellingFinder;

    @BeforeEach
    void setUp() {
        misspellingFinder = new MisspellingSimpleFinder();
    }

    @Test
    void testFindMisspellingsNoResults() {
        String text = "Sample text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "b");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testFindMisspellingsWithResults() {
        String text = "sample text.";
        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("sample", "ejemplo");
        Misspelling misspelling2 = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

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
    void testFindMisspellingsWithResultCaseInsensitive() {
        String text = "Sample Text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("Text", result.getText());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    void testFindMisspellingsWithResultCaseSensitive() {
        String text = "text Text";
        Misspelling misspelling = Misspelling.of("text", true, "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("text", result.getText());
        Assertions.assertEquals(0, result.getStart());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    void testFindMisspellingsWithCompleteWord() {
        String text = "Texto Text";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("Text", result.getText());
        Assertions.assertEquals(6, result.getStart());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    void testFindMisspellingsWithUppercase() {
        String text = "SAMPLE TEXT";
        Misspelling misspelling1 = Misspelling.of("SAMPLE", true, "sample");
        Misspelling misspelling2 = Misspelling.of("text", false, "texto");
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("SAMPLE", result.getText());
        Assertions.assertEquals(0, result.getStart());
        Assertions.assertEquals("SAMPLE", result.getSubtype());
    }

    @Test
    void testFindMisspellingsBetweenUnderscores() {
        String text = "A _Text Text_ _Text_ Text.";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());
        Replacement result = results.get(0);
        Assertions.assertEquals("Text", result.getText());
        Assertions.assertEquals(21, result.getStart());
        Assertions.assertEquals("text", result.getSubtype());
    }

    @Test
    void testFindMisspellingSuggestion() {
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

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertEquals("España", results.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals("domingo", results.get(1).getSuggestions().get(0).getText());
        Assertions.assertEquals("águila", results.get(2).getSuggestions().get(0).getText());
        Assertions.assertEquals("Águila", results.get(3).getSuggestions().get(0).getText());
    }

    @Test
    void testFindMisspellingSuggestionSameWordFirst() {
        String word = "entreno";
        String comment = "entrenó (verbo), entreno (sustantivo)";
        Misspelling misspelling = Misspelling.ofCaseInsensitive(word, comment);
        String text = String.format("Un %s.", word);

        // Fake the update of the misspelling list in the misspelling manager
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(3, results.get(0).getStart());
        Assertions.assertEquals(word, results.get(0).getText());
        Assertions.assertEquals(2, results.get(0).getSuggestions().size());
        Assertions.assertEquals(word, results.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testMisspellingListEmpty() {
        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, EMPTY_MAP));

        Assertions.assertTrue(misspellingFinder.findList("A sample text").isEmpty());
    }

    @Test
    void testFindMisspellingWithDot() {
        String text = "De 23 cms. de altura";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("cms.", "cm");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testFindMisspellingWithNumber() {
        String text = "De 23 m2 de extensión";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("m2", "m²");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }
}
