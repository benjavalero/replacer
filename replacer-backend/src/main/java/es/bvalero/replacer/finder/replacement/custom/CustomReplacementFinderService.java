package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.page.review.PageReviewOptions;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementFinderService implements FinderService<Replacement> {

    @Override
    public Set<Replacement> find(WikipediaPage page) {
        throw new IllegalCallerException();
    }

    @Override
    public Iterable<Replacement> findIterable(WikipediaPage page) {
        throw new IllegalCallerException();
    }

    @Override
    public Iterable<Finder<Replacement>> getFinders() {
        throw new IllegalCallerException();
    }

    public Iterable<Replacement> findCustomReplacements(WikipediaPage page, PageReviewOptions customOptions) {
        final CustomReplacementFinder finder = CustomReplacementFinder.of(convertOptions(customOptions));
        return findIterable(page, Collections.singleton(finder));
    }

    private CustomOptions convertOptions(PageReviewOptions options) {
        assert options.getType().getKind() == ReplacementKind.CUSTOM;
        String subtype = options.getType().getSubtype();
        Boolean cs = options.getCs();
        String suggestion = options.getSuggestion();
        assert cs != null && suggestion != null;
        return CustomOptions.of(subtype, cs, suggestion);
    }
}
