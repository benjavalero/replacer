package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
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
class ArticleReviewNoTypeService extends ArticleReviewService {
    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Override
    String buildReplacementCacheKey(ArticleReviewOptions options) {
        return options.getLang().getCode();
    }

    @Override
    List<Integer> findArticleIdsToReview(ArticleReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        long randomStart = replacementRepository.findRandomStart(CACHE_SIZE, options.getLang().getCode());
        return replacementRepository.findRandomArticleIdsToReview(options.getLang().getCode(), randomStart, pagination);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article, ArticleReviewOptions options) {
        List<Replacement> replacements = replacementFindService.findReplacements(
            article.getContent(),
            article.getLang()
        );

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.debug("Update article replacements in database");
        replacementIndexService.indexArticleReplacements(
            article.getId(),
            article.getLang(),
            replacements.stream().map(article::convertReplacementToIndexed).collect(Collectors.toList())
        );

        return replacements;
    }
}
