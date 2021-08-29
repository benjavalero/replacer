package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;

/** Decorator to report to Check Wikipedia */
public abstract class CosmeticCheckedFinder implements CosmeticFinder {

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

    protected abstract CheckWikipediaAction getCheckWikipediaAction();

    private boolean applyAction(FinderPage page) {
        checkWikipediaService.reportFix(page.getLang(), page.getTitle(), getCheckWikipediaAction());
        return true;
    }
}
