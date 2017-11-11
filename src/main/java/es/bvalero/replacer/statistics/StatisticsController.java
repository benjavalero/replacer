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
        // TODO Count only the ones not reviewed
        Long count = potentialErrorRepository.count();
        LOGGER.info("Potential errors found: " + count);
        return count;
    }

    @RequestMapping(value = "/statistics/count/articles")
    Long countArticles() {
        LOGGER.info("Count articles...");
        // TODO Count only the ones not reviewed
        Long count = articleRepository.count();
        LOGGER.info("Articles found: " + count);
        return count;
    }

    @RequestMapping(value = "/statistics/count/misspellings")
    List<Object[]> listMisspellings() {
        LOGGER.info("Listing misspellings...");
        // TODO Count only the ones not reviewed
        List<Object[]> list = potentialErrorRepository.findMisspellingsGrouped();
        LOGGER.info("Misspelling list found: " + list.size());
        return list;
    }

}
