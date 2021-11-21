package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import java.util.Set;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComposedMisspellingLoader extends ListingLoader<ComposedMisspelling> {

    @Autowired
    private ListingFinder listingFinder;

    @Setter // For testing
    @Autowired
    private ComposedMisspellingParser composedMisspellingParser;

    @Override
    public String getLabel() {
        return "Composed Misspelling";
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
