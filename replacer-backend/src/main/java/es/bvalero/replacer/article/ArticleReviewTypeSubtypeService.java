package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
class ArticleReviewTypeSubtypeService extends ArticleReviewCachedService {

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ReplacementCountService replacementCountService;

    private String type;
    private String subtype;

    Optional<ArticleReview> findRandomArticleReview(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
        return findRandomArticleReview();
    }

    @Override
    String buildReplacementCacheKey() {
        return String.format("%s-%s", type, subtype);
    }

    @Override
    List<Integer> findArticleIdsToReview() {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        List<Integer> articleIds = replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(type, subtype, pagination);

        if (articleIds.isEmpty()) {
            // If finally there are no results empty the cached count for the replacement
            // No need to check if there exists something cached
            replacementCountService.removeCachedReplacementCount(type, subtype);
        }

        return articleIds;
    }

    Optional<ArticleReview> getArticleReview(int articleId, String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
        return getArticleReview(articleId);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article) {
        List<Replacement> replacements = super.findAllReplacements(article);

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        replacements = filterReplacementsByTypeAndSubtype(replacements);
        LOGGER.debug("Final replacements found in text after filtering: {}", replacements.size());

        return replacements;
    }

    private List<Replacement> filterReplacementsByTypeAndSubtype(List<Replacement> replacements) {
        return replacements.stream()
                .filter(replacement -> replacement.getType().equals(type) && replacement.getSubtype().equals(subtype))
                .collect(Collectors.toList());
    }

}
