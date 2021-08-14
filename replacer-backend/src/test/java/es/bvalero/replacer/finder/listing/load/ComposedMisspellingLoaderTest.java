package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ComposedMisspellingLoaderTest {

    @Mock
    private ListingFinder listingFinder;

    @Mock
    private ComposedMisspellingParser composedMisspellingParser;

    @InjectMocks
    private ComposedMisspellingLoader composedMisspellingLoader;

    @BeforeEach
    public void setUp() {
        composedMisspellingLoader = new ComposedMisspellingLoader();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testLoad() throws ReplacerException {
        Mockito.when(listingFinder.getComposedMisspellingListing(Mockito.any(WikipediaLanguage.class))).thenReturn("");

        composedMisspellingLoader.load();

        Mockito.verify(listingFinder).getComposedMisspellingListing(WikipediaLanguage.SPANISH);
        Mockito.verify(composedMisspellingParser, Mockito.atLeastOnce()).parseListing(Mockito.anyString());
    }
}
