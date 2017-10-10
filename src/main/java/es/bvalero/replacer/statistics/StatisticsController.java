package es.bvalero.replacer.statistics;

import es.bvalero.replacer.article.ArticleRepository;
import es.bvalero.replacer.article.PotentialErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatisticsController {

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @RequestMapping(value = "/statistics/count/potentialErrors")
    Long countPotentialErrors() {
        // TODO Count only the ones not reviewed
        return potentialErrorRepository.count();
    }

    @RequestMapping(value = "/statistics/count/articles")
    Long countArticles() {
        // TODO Count only the ones not reviewed
        return articleRepository.count();
    }

    @RequestMapping(value = "/statistics/count/misspellings")
    List<Object[]> listMisspellings() {
        // TODO Count only the ones not reviewed
        // TODO Return all the misspellings and display a paginated table
        return potentialErrorRepository.findMisspellingsGrouped().subList(0, 100);
    }

}
