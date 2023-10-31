package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class FalsePositiveLoader extends ListingLoader<FalsePositive> {

    // Dependency injection
    private final ListingFinder listingFinder;
    private final FalsePositiveParser falsePositiveParser;

    public FalsePositiveLoader(ListingFinder listingFinder, FalsePositiveParser falsePositiveParser) {
        this.listingFinder = listingFinder;
        this.falsePositiveParser = falsePositiveParser;
    }

    @Override
    public String getLabel() {
        return "False Positive";
    }

    @Override
    public String findListingByLang(WikipediaLanguage lang) throws ReplacerException {
        return listingFinder.getFalsePositiveListing(lang);
    }

    @Override
    public Set<FalsePositive> parseListing(String listingContent) {
        return falsePositiveParser.parseListing(listingContent);
    }
}
