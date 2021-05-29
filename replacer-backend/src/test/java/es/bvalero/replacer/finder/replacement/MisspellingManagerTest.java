package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.common.ListingLoader;
import es.bvalero.replacer.replacement.ReplacementService;
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

class MisspellingManagerTest {

    @Mock
    private ListingLoader listingLoader;

    @Mock
    private ReplacementService replacementService;

    @InjectMocks
    private MisspellingManager misspellingManager;

    @BeforeEach
    public void setUp() {
        misspellingManager = new MisspellingManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testParseMisspellingListText() {
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
    void testDeleteObsoleteMisspellings() {
        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("A", "B");
        Misspelling misspelling2 = Misspelling.ofCaseInsensitive("B", "C");
        SetValuedMap<WikipediaLanguage, Misspelling> map1 = new HashSetValuedHashMap<>();
        map1.putAll(WikipediaLanguage.SPANISH, Arrays.asList(misspelling1, misspelling2));
        misspellingManager.setItems(map1);

        Mockito
            .verify(replacementService, Mockito.times(0))
            .deleteToBeReviewedBySubtype(Mockito.any(WikipediaLanguage.class), Mockito.anyString(), Mockito.anySet());

        Misspelling misspelling3 = Misspelling.ofCaseInsensitive("C", "D");
        SetValuedMap<WikipediaLanguage, Misspelling> map2 = new HashSetValuedHashMap<>();
        map2.putAll(WikipediaLanguage.SPANISH, Arrays.asList(misspelling2, misspelling3));
        misspellingManager.setItems(map2);

        Mockito
            .verify(replacementService, Mockito.times(1))
            .deleteToBeReviewedBySubtype(
                WikipediaLanguage.SPANISH,
                ReplacementType.MISSPELLING_SIMPLE,
                Collections.singleton("A")
            );
    }

    @Test
    void testUpdate() throws ReplacerException {
        Mockito.when(listingLoader.getMisspellingListPageContent(Mockito.any(WikipediaLanguage.class))).thenReturn("");

        misspellingManager.scheduledItemListUpdate();

        Mockito.verify(listingLoader).getMisspellingListPageContent(WikipediaLanguage.SPANISH);
    }
}
