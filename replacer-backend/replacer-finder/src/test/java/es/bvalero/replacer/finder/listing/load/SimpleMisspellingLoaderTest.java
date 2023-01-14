package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SimpleMisspellingLoaderTest {

    @Mock
    private ListingFinder listingFinder;

    @Mock
    private SimpleMisspellingParser simpleMisspellingParser;

    @InjectMocks
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @BeforeEach
    public void setUp() {
        simpleMisspellingLoader = new SimpleMisspellingLoader();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoad() throws ReplacerException {
        when(listingFinder.getSimpleMisspellingListing(any(WikipediaLanguage.class))).thenReturn("");

        simpleMisspellingLoader.load();

        verify(listingFinder).getSimpleMisspellingListing(WikipediaLanguage.SPANISH);
        verify(simpleMisspellingParser, atLeastOnce()).parseListing(anyString());
    }
}