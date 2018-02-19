package es.bvalero.replacer.statistics;

import es.bvalero.replacer.article.ArticleRepository;
import es.bvalero.replacer.article.PotentialErrorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatisticsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @RequestMapping(value = "/statistics/count/potentialErrors")
    Long countPotentialErrors() {
        LOGGER.info("Count potential errors...");
        Long count = potentialErrorRepository.countNotReviewed();
        LOGGER.info("Potential errors found: {}", count);
        return count;
    }

    @RequestMapping(value = "/statistics/count/articles")
    Long countArticlesNotReviewed() {
        LOGGER.info("Count articles not reviewed...");
        Long count = articleRepository.countByReviewDateNull();
        LOGGER.info("Articles not reviewed found: {}", count);
        return count;
    }

    @RequestMapping(value = "/statistics/count/articles-reviewed")
    Long countArticlesReviewed() {
        LOGGER.info("Count articles reviewed...");
        Long count = articleRepository.countByReviewDateNotNull();
        LOGGER.info("Articles reviewed found: {}", count);
        return count;
    }

    @RequestMapping(value = "/statistics/count/misspellings")
    List<Object[]> listMisspellings() {
        LOGGER.info("Listing misspellings...");
        List<Object[]> list = potentialErrorRepository.findMisspellingsGrouped();
        LOGGER.info("Misspelling list found: {}", list.size());
        return list;
    }

}
