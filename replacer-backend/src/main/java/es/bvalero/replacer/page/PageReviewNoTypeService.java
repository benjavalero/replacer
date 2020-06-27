package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageReviewNoTypeService extends PageReviewService {
    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Getter
    @Setter
    @Resource
    private List<String> ignorableTemplates;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
        return options.getLang().getCode();
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        long randomStart = replacementRepository.findRandomStart(CACHE_SIZE, options.getLang().getCode());
        long totalResults = replacementRepository.countByLangAndReviewerIsNull(options.getLang().getCode());
        List<Integer> pageIds = replacementRepository.findRandomPageIdsToReview(
            options.getLang().getCode(),
            randomStart,
            pagination
        );
        return new PageSearchResult(totalResults, pageIds);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article, PageReviewOptions options) {
        List<Replacement> replacements = replacementFindService.findReplacements(
            article.getContent(),
            article.getLang()
        );

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.debug("Update article replacements in database");
        replacementIndexService.indexPageReplacements(
            article.getId(),
            article.getLang(),
            replacements.stream().map(article::convertReplacementToIndexed).collect(Collectors.toList())
        );

        return replacements;
    }
}
