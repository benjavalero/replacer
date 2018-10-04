package es.bvalero.replacer.statistics;

import es.bvalero.replacer.article.MisspellingCount;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.ReplacementRepository;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatisticsController {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @RequestMapping("/statistics/count/replacements")
    Long countPotentialErrors() {
        LOGGER.info("Count potential errors...");
        Long count = replacementRepository.count();
        LOGGER.info("Potential errors found: {}", count);
        return count;
    }

    @RequestMapping("/statistics/count/articles")
    Long countArticlesNotReviewed() {
        LOGGER.info("Count articles not reviewed...");
        Long count = articleRepository.countByReviewDateNull();
        LOGGER.info("Articles not reviewed found: {}", count);
        return count;
    }

    @RequestMapping("/statistics/count/articles-reviewed")
    Long countArticlesReviewed() {
        LOGGER.info("Count articles reviewed...");
        Long count = articleRepository.countByReviewDateNotNull();
        LOGGER.info("Articles reviewed found: {}", count);
        return count;
    }

    @RequestMapping("/statistics/count/misspellings")
    List<MisspellingCount> listMisspellings() {
        LOGGER.info("Listing misspellings...");
        List<MisspellingCount> list = replacementRepository.findMisspellingsGrouped();
        LOGGER.info("Misspelling list found: {}", list.size());
        return list;
    }

}
