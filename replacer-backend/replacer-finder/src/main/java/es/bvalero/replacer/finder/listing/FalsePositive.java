package es.bvalero.replacer.finder.listing;

import lombok.Value;

@Value(staticConstructor = "of")
public class FalsePositive implements ListingItem {

    String expression;

    @Override
    public String getKey() {
        return this.expression;
    }
}
