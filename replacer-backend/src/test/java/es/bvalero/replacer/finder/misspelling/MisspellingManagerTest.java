package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MisspellingManagerTest {
    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ReplacementRepository replacementRepository;

    @InjectMocks
    private MisspellingManager misspellingManager;

    @BeforeEach
    public void setUp() {
        misspellingManager = new MisspellingManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseMisspellingListText() {
        String misspellingListText =
            "Text\n\n" +
            "A||B\n" + // No starting whitespace
            " C|cs|D\n" +
            " E|CS|F\n" +
            " G|H\n" + // Bad formatted
            " I||J\n" +
            " I||J\n" + // Duplicated
            " k||k\n" +
            " k||M\n"; // Duplicated but different comment

        Collection<Misspelling> misspellings = misspellingManager.parseItemsText(misspellingListText);
        Assertions.assertEquals(4, misspellings.size());
        Assertions.assertTrue(misspellings.contains(Misspelling.of("C", true, "D")));
        Assertions.assertTrue(misspellings.contains(Misspelling.of("E", true, "F")));
        Assertions.assertTrue(misspellings.contains(Misspelling.of("I", false, "J")));
        Assertions.assertTrue(misspellings.contains(Misspelling.of("k", false, "k")));
    }

    @Test
    public void testParseValidMisspellingWords() {
        String misspellingListText =
            " aguila||águila\n" +
            " m2||m²\n" + // Not valid with numbers
            " Castilla-León||Castilla y León\n" + // Valid with dashes
            " CD's||CD\n" + // Valid with single quotes
            " n°||n.º\n" + // Not valid with degree symbol
            " nº||n.º\n" + // Not valid with superscript
            " cm.||cm\n"; // Not valid with dots

        Collection<Misspelling> misspellings = misspellingManager.parseItemsText(misspellingListText);
        Assertions.assertEquals(3, misspellings.size());
        Assertions.assertTrue(misspellings.contains(Misspelling.ofCaseInsensitive("aguila", "águila")));
        Assertions.assertTrue(misspellings.contains(Misspelling.ofCaseInsensitive("Castilla-León", "Castilla y León")));
        Assertions.assertTrue(misspellings.contains(Misspelling.ofCaseInsensitive("CD's", "CD")));
    }

    @Test
    public void testDeleteObsoleteMisspellings() {
        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("A", "B");
        Misspelling misspelling2 = Misspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, Misspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.SPANISH, Arrays.asList(misspelling1, misspelling2));
        misspellingManager.setItems(map1);

        Mockito.verify(replacementRepository, Mockito.times(0)).deleteByLangAndSubtypeIn(Mockito.anyString(), Mockito.anySet());

        Misspelling misspelling3 = Misspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, Misspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.SPANISH, Arrays.asList(misspelling2, misspelling3));
        misspellingManager.setItems(map2);

        Mockito.verify(replacementRepository, Mockito.times(1)).deleteByLangAndSubtypeIn(WikipediaLanguage.SPANISH.getCode(), Collections.singleton("A"));
    }

    @Test
    public void testUpdate() throws ReplacerException {
        Mockito
            .when(wikipediaService.getMisspellingListPageContent(Mockito.any(WikipediaLanguage.class)))
            .thenReturn("");

        misspellingManager.update();

        Mockito.verify(wikipediaService).getMisspellingListPageContent(WikipediaLanguage.SPANISH);
    }
}
