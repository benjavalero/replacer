package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Loggable
@RestController
@RequestMapping("api/replacements")
public class ReplacementController {

    @Autowired
    private ReplacementCountService replacementCountService;

    @GetMapping(value = "/count", params = "reviewed=true")
    public Long countReplacementsReviewed(@RequestParam WikipediaLanguage lang) {
        return replacementCountService.countReplacementsReviewed(lang);
    }

    @GetMapping(value = "/count", params = "reviewed=false")
    public Long countReplacementsNotReviewed(@RequestParam WikipediaLanguage lang) {
        return replacementCountService.countReplacementsNotReviewed(lang);
    }

    @GetMapping(value = "/count", params = { "reviewed=true", "grouped" })
    public List<ReviewerCount> countReplacementsGroupedByReviewer(@RequestParam WikipediaLanguage lang) {
        return replacementCountService.countReplacementsGroupedByReviewer(lang);
    }

    @GetMapping(value = "/count", params = { "reviewed=false", "grouped" })
    public List<TypeCount> countReplacementsGroupedByType(@RequestParam WikipediaLanguage lang) {
        return replacementCountService.getCachedReplacementTypeCounts(lang);
    }
}
