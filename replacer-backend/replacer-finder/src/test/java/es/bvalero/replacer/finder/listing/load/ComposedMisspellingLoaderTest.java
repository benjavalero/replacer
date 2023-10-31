package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComposedMisspellingLoaderTest {

    // Dependency injection
    private ListingFinder listingFinder;
    private ComposedMisspellingParser composedMisspellingParser;

    private ComposedMisspellingLoader composedMisspellingLoader;

    @BeforeEach
    public void setUp() {
        listingFinder = mock(ListingFinder.class);
        composedMisspellingParser = mock(ComposedMisspellingParser.class);
        composedMisspellingLoader = new ComposedMisspellingLoader(listingFinder, composedMisspellingParser);
    }

    @Test
    void testLoad() throws ReplacerException {
        when(listingFinder.getComposedMisspellingListing(any(WikipediaLanguage.class))).thenReturn("");

        composedMisspellingLoader.load();

        verify(listingFinder).getComposedMisspellingListing(WikipediaLanguage.SPANISH);
        verify(composedMisspellingParser, atLeastOnce()).parseListing(anyString());
    }
}
