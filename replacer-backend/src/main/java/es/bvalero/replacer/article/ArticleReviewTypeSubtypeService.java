package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ArticleReviewTypeSubtypeService extends ArticleReviewCachedService {
    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ReplacementCountService replacementCountService;

    @Override
    List<Integer> findArticleIdsToReview(ArticleReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        List<Integer> articleIds = replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
            options.getLang().getCode(),
            options.getType(),
            options.getSubtype(),
            pagination
        );

        if (articleIds.isEmpty()) {
            // If finally there are no results empty the cached count for the replacement
            // No need to check if there exists something cached
            replacementCountService.removeCachedReplacementCount(options.getLang(), options.getType(), options.getSubtype());
        }

        return articleIds;
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article, ArticleReviewOptions options) {
        List<Replacement> replacements = findAllReplacements(article);

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        replacements = filterReplacementsByTypeAndSubtype(replacements, options.getType(), options.getSubtype());
        LOGGER.debug("Final replacements found in text after filtering: {}", replacements.size());

        return replacements;
    }

    private List<Replacement> filterReplacementsByTypeAndSubtype(
        List<Replacement> replacements,
        String type,
        String subtype
    ) {
        return replacements
            .stream()
            .filter(replacement -> replacement.getType().equals(type) && replacement.getSubtype().equals(subtype))
            .collect(Collectors.toList());
    }
}
