package es.bvalero.replacer.web;

import es.bvalero.replacer.domain.Count;
import es.bvalero.replacer.service.ReplacementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatisticsController {

    @Autowired
    private ReplacementService replacementService;

    @RequestMapping(value = "/count/misspellings")
    Integer countMisspellings() {
        return replacementService.countMisspellings();
    }

    @RequestMapping(value = "/count/articles")
    Integer countArticles() {
        return replacementService.countArticles();
    }

    @RequestMapping(value = "/list/misspellings")
    List<Count> listMisspellings() {
        return replacementService.findMisspellingsGrouped();
    }

}
