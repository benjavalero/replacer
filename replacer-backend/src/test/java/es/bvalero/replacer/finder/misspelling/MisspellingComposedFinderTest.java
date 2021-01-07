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

class MisspellingComposedFinderTest {

    private static final SetValuedMap<WikipediaLanguage, Misspelling> EMPTY_MAP = new HashSetValuedHashMap<>();

    private MisspellingComposedFinder misspellingComposedFinder;

    @BeforeEach
    public void setUp() {
        misspellingComposedFinder = new MisspellingComposedFinder();
    }

    @Test
    void testFindComposedMisspelling() {
        String text = "Y aún así vino.";
        Misspelling simple = Misspelling.ofCaseInsensitive("aún", "aun");
        Misspelling composed = Misspelling.ofCaseInsensitive("aún así", "aun así");
        Set<Misspelling> misspellings = new HashSet<>(Arrays.asList(simple, composed));
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellings);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingComposedFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        Replacement result1 = results.get(0);
        Assertions.assertEquals("aún así", result1.getText());
        Assertions.assertEquals("aún así", result1.getSubtype());
        Assertions.assertEquals("aun así", result1.getSuggestions().get(0).getText());
    }

    @Test
    void testComposedMisspellingListEmpty() {
        // Fake the update of the misspelling list in the misspelling manager
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, EMPTY_MAP));

        Assertions.assertTrue(misspellingComposedFinder.findList("A sample text", WikipediaLanguage.SPANISH).isEmpty());
    }

    @Test
    void testFindMisspellingWithDot() {
        String text = "Aún mas. Masa.";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("mas.", "más.");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingComposedFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        Replacement result1 = results.get(0);
        Assertions.assertEquals("mas.", result1.getText());
        Assertions.assertEquals("mas.", result1.getSubtype());
        Assertions.assertEquals("más.", result1.getSuggestions().get(0).getText());
    }

    @Test
    void testFindMisspellingWithComma() {
        String text = "Más aun, dos.";
        Misspelling misspelling = Misspelling.ofCaseInsensitive("aun,", "aún,");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingComposedFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        Replacement result1 = results.get(0);
        Assertions.assertEquals("aun,", result1.getText());
        Assertions.assertEquals("aun,", result1.getSubtype());
        Assertions.assertEquals("aún,", result1.getSuggestions().get(0).getText());
    }

    @Test
    void testFindMisspellingWithNumber() {
        String text = "En Rio 2016.";
        Misspelling misspelling = Misspelling.of("Rio 2016", true, "Río 2016");
        Set<Misspelling> misspellingSet = Collections.singleton(misspelling);
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellingSet);

        // Fake the update of the misspelling list in the misspelling manager
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Replacement> results = misspellingComposedFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        Replacement result1 = results.get(0);
        Assertions.assertEquals("Rio 2016", result1.getText());
        Assertions.assertEquals("Rio 2016", result1.getSubtype());
        Assertions.assertEquals("Río 2016", result1.getSuggestions().get(0).getText());
    }
}
