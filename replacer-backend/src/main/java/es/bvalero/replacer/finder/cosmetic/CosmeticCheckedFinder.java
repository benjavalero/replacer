package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.FinderPage;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;

/** Decorator to report to Check Wikipedia */
abstract class CosmeticCheckedFinder implements CosmeticFinder {

    private CheckWikipediaService checkWikipediaService;

    @Autowired
    public final void setCheckWikipediaService(CheckWikipediaService checkWikipediaService) {
        this.checkWikipediaService = checkWikipediaService;
    }

    @Override
    public Iterable<Cosmetic> find(FinderPage page) {
        // Trick: use iterable filter with no conversion at all but decoration
        return IterableUtils.filteredIterable(CosmeticFinder.super.find(page), cosmetic -> this.applyAction(page));
    }

    abstract int getFixId();

    private boolean applyAction(FinderPage page) {
        checkWikipediaService.reportFix(page.getLang(), page.getTitle(), getFixId());
        return true;
    }
}
