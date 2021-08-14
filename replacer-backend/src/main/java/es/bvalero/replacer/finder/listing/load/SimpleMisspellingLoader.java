package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.Set;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpleMisspellingLoader extends ListingLoader<SimpleMisspelling> {

    @Autowired
    private ListingFinder listingFinder;

    @Setter // For testing
    @Autowired
    private SimpleMisspellingParser simpleMisspellingParser;

    @Override
    public String getLabel() {
        return "Simple Misspelling";
    }

    @Override
    public String findListingByLang(WikipediaLanguage lang) throws ReplacerException {
        return listingFinder.getSimpleMisspellingListing(lang);
    }

    @Override
    public Set<SimpleMisspelling> parseListing(String listingContent) {
        return simpleMisspellingParser.parseListing(listingContent);
    }
}
