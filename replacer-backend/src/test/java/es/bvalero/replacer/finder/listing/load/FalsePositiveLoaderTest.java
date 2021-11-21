package es.bvalero.replacer.finder.listing.load;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FalsePositiveLoaderTest {

    @Mock
    private ListingFinder listingFinder;

    @Mock
    private FalsePositiveParser falsePositiveParser;

    @InjectMocks
    private FalsePositiveLoader falsePositiveLoader;

    @BeforeEach
    public void setUp() {
        falsePositiveLoader = new FalsePositiveLoader();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testLoad() throws ReplacerException {
        when(listingFinder.getFalsePositiveListing(any(WikipediaLanguage.class))).thenReturn("");

        falsePositiveLoader.load();

        verify(listingFinder).getFalsePositiveListing(WikipediaLanguage.SPANISH);
        verify(falsePositiveParser, atLeastOnce()).parseListing(anyString());
    }
}
