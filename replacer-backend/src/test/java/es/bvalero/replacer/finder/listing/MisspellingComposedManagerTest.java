package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class MisspellingComposedManagerTest {

    @Mock
    private ListingContentService listingContentService;

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
            .when(listingContentService.getComposedMisspellingListingContent(Mockito.any(WikipediaLanguage.class)))
            .thenReturn("");

        misspellingComposedManager.scheduledItemListUpdate();

        Mockito.verify(listingContentService).getComposedMisspellingListingContent(WikipediaLanguage.SPANISH);
    }
}
