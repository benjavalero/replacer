package es.bvalero.replacer.review.find;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.domain.FinderResult;
import es.bvalero.replacer.common.domain.WikipediaSearchResult;
import es.bvalero.replacer.page.findreplacement.PageReplacementFinder;
import es.bvalero.replacer.repository.*;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PageReviewCustomFinder extends PageReviewFinder {

    @Autowired
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Autowired
    private PageReplacementFinder pageReplacementFinder;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Initialize search
        int offset = getCachedResult(options).map(PageSearchResult::getOffset).orElse(0);

        WikipediaSearchResult searchResult = findWikipediaResults(options, offset);
        final int totalWikipediaResults = searchResult.getTotal();
        int totalToReview = searchResult.getTotal();
        final List<Integer> pageIds = new LinkedList<>(searchResult.getPageIds());

        String subtype = options.getType().getSubtype();
        boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
        assert options.getType().getKind() == ReplacementKind.CUSTOM;

        // Calculate this out of the loop only if needed the first time
        List<Integer> reviewedIds = new ArrayList<>();
        if (!pageIds.isEmpty()) {
            reviewedIds.addAll(customRepository.findPageIdsReviewed(options.getLang(), subtype, cs));
        }

        while (totalToReview >= 0) {
            // Discard the pages already reviewed
            // For the moment we don't check the positions of the replacements,
            // which means that once a custom replacement is reviewed for a page it is done for ever.
            for (Integer reviewId : reviewedIds) {
                if (pageIds.remove(reviewId)) {
                    totalToReview--;
                }
            }

            if (pageIds.isEmpty()) {
                offset += getCacheSize();
                if (offset >= totalWikipediaResults) {
                    LOGGER.debug("All results retrieved from Wikipedia are already reviewed");
                    return PageSearchResult.ofEmpty();
                }

                searchResult = findWikipediaResults(options, offset);
                // For simplicity's sake we assume the number of total results is the same
                pageIds.clear();
                pageIds.addAll(searchResult.getPageIds());
            } else {
                int nextOffset = offset + getCacheSize();
                return PageSearchResult.of(totalToReview, pageIds, nextOffset);
            }
        }

        return PageSearchResult.ofEmpty();
    }

    private WikipediaSearchResult findWikipediaResults(ReviewOptions options, int offset) {
        String subtype = options.getType().getSubtype();
        Boolean cs = options.getCs();
        assert cs != null;
        return wikipediaPageRepository.findByContent(
            options.getLang(),
            indexableNamespaces.stream().map(WikipediaNamespace::valueOf).collect(Collectors.toUnmodifiableSet()),
            subtype,
            cs,
            offset,
            getCacheSize()
        );
    }

    @Override
    boolean stopWhenEmptyTotal() {
        return true;
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        Collection<Replacement> customReplacements = pageReplacementFinder.findCustomReplacements(page, options);

        // If no custom replacements are found then we don't want to review the page
        if (customReplacements.isEmpty()) {
            return Collections.emptyList();
        }

        // We add the found replacements to the database but as not reviewed
        // Add the page to the database in case it doesn't exist yet
        if (pageIndexRepository.findPageById(page.getId()).isEmpty()) {
            pageRepository.addPages(List.of(buildNewPage(page)));
        }
        // We want to review the page every time in case anything has changed
        for (Replacement replacement : customReplacements) {
            customRepository.addCustom(mapPageCustomReplacement(page, options, replacement));
        }

        // Add the custom replacements to the standard ones preferring the custom ones
        // Return the merged collection as a TreeSet to keep the order and discard duplicates
        // We also check there are no replacements containing others
        Collection<Replacement> merged = Stream
            .of(customReplacements, replacements)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(TreeSet::new));
        FinderResult.removeNested(merged);
        return merged;
    }

    private PageModel buildNewPage(WikipediaPage page) {
        return PageModel
            .builder()
            .lang(page.getId().getLang().getCode())
            .pageId(page.getId().getPageId())
            .title(page.getTitle())
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .replacements(Collections.emptyList())
            .build();
    }

    private CustomModel mapPageCustomReplacement(WikipediaPage page, ReviewOptions options, Replacement replacement) {
        return CustomModel
            .builder()
            .lang(page.getId().getLang().getCode())
            .pageId(page.getId().getPageId())
            .replacement(options.getType().getSubtype())
            .cs((byte) (Boolean.TRUE.equals(options.getCs()) ? 1 : 0))
            .position(replacement.getStart())
            .context(replacement.getContext(page))
            .build();
    }
}
