package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.FalsePositive;
import org.springframework.stereotype.Component;

@Component
public class FalsePositiveParser extends ListingParser<FalsePositive> {

    @Override
    public FalsePositive parseItemLine(String itemLine) {
        return FalsePositive.of(itemLine.trim());
    }
}
