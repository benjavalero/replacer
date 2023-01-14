package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import java.util.Set;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FalsePositiveLoader extends ListingLoader<FalsePositive> {

    @Autowired
    private ListingFinder listingFinder;

    @Setter(onMethod_ = @TestOnly)
    @Autowired
    private FalsePositiveParser falsePositiveParser;

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