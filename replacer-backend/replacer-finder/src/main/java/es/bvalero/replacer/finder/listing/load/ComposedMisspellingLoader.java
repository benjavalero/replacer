package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ComposedMisspellingLoader extends ListingLoader<ComposedMisspelling> {

    public static final String LABEL_COMPOSED_MISSPELLING = "Composed Misspelling";

    // Dependency injection
    private final ListingFinder listingFinder;
    private final ComposedMisspellingParser composedMisspellingParser;

    public ComposedMisspellingLoader(ListingFinder listingFinder, ComposedMisspellingParser composedMisspellingParser) {
        this.listingFinder = listingFinder;
        this.composedMisspellingParser = composedMisspellingParser;
    }

    @Override
    public String getLabel() {
        return LABEL_COMPOSED_MISSPELLING;
    }

    @Override
    public String findListingByLang(WikipediaLanguage lang) throws ReplacerException {
        return listingFinder.getComposedMisspellingListing(lang);
    }

    @Override
    public Set<ComposedMisspelling> parseListing(String listingContent) {
        return composedMisspellingParser.parseListing(listingContent);
    }
}
