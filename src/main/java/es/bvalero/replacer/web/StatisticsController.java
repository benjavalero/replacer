package es.bvalero.replacer.web;

import es.bvalero.replacer.domain.Count;
import es.bvalero.replacer.persistence.ReplacementDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatisticsController {

    @Autowired
    private ReplacementDao replacementDao;

    @RequestMapping(value = "/count/misspellings")
    Integer countMisspellings() {
        return replacementDao.countMisspellings();
    }

    @RequestMapping(value = "/count/articles")
    Integer countArticles() {
        return replacementDao.countArticles();
    }

    @RequestMapping(value = "/list/misspellings")
    List<Count> listMisspellings() {
        return replacementDao.findMisspellingsGrouped();
    }

}
