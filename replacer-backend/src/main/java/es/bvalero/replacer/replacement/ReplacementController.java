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
@RequestMapping("api/replacement")
public class ReplacementController {
    @Autowired
    private ReplacementCountService replacementCountService;

    @GetMapping(value = "/count")
    public Long countReplacements() {
        Long count = replacementCountService.countAllReplacements();
        LOGGER.info("GET Count replacements. Result: {}", count);
        return count;
    }

    @GetMapping(value = "/count/to-review")
    public Long countReplacementsToReview() {
        Long count = replacementCountService.countReplacementsToReview();
        LOGGER.info("GET Count not reviewed. Results: {}", count);
        return count;
    }

    @GetMapping("/count/reviewed")
    public Long countReplacementsReviewed() {
        Long count = replacementCountService.countReplacementsReviewed();
        LOGGER.info("GET Count reviewed replacements. Result: {}", count);
        return count;
    }

    @GetMapping(value = "/count/reviewed/grouped")
    public List<ReviewerCount> countReplacementsGroupedByReviewer() {
        List<ReviewerCount> list = replacementCountService.countReplacementsGroupedByReviewer();
        LOGGER.info("GET Count grouped by reviewer. Result Size: {}", list.size());
        return list;
    }

    @GetMapping(value = "/count/grouped")
    public List<TypeCount> findReplacementCount(@RequestParam(required = false) WikipediaLanguage lang) {
        if (lang == null) {
            // Default value
            lang = WikipediaLanguage.SPANISH;
        }
        List<TypeCount> list = replacementCountService.findReplacementCount(lang);
        LOGGER.info("GET Grouped replacement count. Result Size: {}", list.size());
        Collections.sort(list);
        return list;
    }
}
