package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.common.ListingLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class MisspellingComposedManagerTest {

    @Mock
    private ListingLoader listingLoader;

    @InjectMocks
    private MisspellingComposedManager misspellingComposedManager;

    @BeforeEach
    public void setUp() {
        misspellingComposedManager = new MisspellingComposedManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testUpdate() throws ReplacerException {
        Mockito
            .when(listingLoader.getComposedMisspellingListPageContent(Mockito.any(WikipediaLanguage.class)))
            .thenReturn("");

        misspellingComposedManager.scheduledItemListUpdate();

        Mockito.verify(listingLoader).getComposedMisspellingListPageContent(WikipediaLanguage.SPANISH);
    }
}
