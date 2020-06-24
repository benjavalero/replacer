package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/replacements")
public class ReplacementController {
    @Autowired
    private ReplacementCountService replacementCountService;

    @GetMapping(value = "/count", params = "reviewed=false")
    public Long countReplacementsToReview(@RequestParam WikipediaLanguage lang) {
        Long count = replacementCountService.countReplacementsToReview(lang);
        LOGGER.info("GET Count not reviewed ({}). Result: {}", lang, count);
        return count;
    }

    @GetMapping(value = "/count", params = "reviewed=true")
    public Long countReplacementsReviewed(@RequestParam WikipediaLanguage lang) {
        Long count = replacementCountService.countReplacementsReviewed(lang);
        LOGGER.info("GET Count reviewed replacements ({}). Result: {}", lang, count);
        return count;
    }

    @GetMapping(value = "/count", params = { "reviewed=true", "grouped" })
    public List<ReviewerCount> countReplacementsGroupedByReviewer(@RequestParam WikipediaLanguage lang) {
        List<ReviewerCount> list = replacementCountService.countReplacementsGroupedByReviewer(lang);
        LOGGER.info("GET Count grouped by reviewer ({}). Result Size: {}", lang, list.size());
        return list;
    }

    @GetMapping(value = "/count", params = { "reviewed=false", "grouped" })
    public List<TypeCount> findReplacementCount(@RequestParam WikipediaLanguage lang) {
        List<TypeCount> list = replacementCountService.findReplacementCount(lang);
        LOGGER.info("GET Grouped replacement count ({}). Result Size: {}", lang, list.size());
        Collections.sort(list);
        return list;
    }
}
