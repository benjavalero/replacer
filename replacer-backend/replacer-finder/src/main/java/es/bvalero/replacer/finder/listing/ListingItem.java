package es.bvalero.replacer.finder.listing;

public interface ListingItem {
    // To identify an item in order to detect duplicates
    String getKey();
}
