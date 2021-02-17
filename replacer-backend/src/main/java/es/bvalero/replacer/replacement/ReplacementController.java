package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Loggable(skipResult = true)
@RestController
@RequestMapping("api/replacements")
public class ReplacementController {

    @Autowired
    private ReplacementService replacementService;

    @GetMapping(value = "/count", params = "reviewed=true")
    public Long countReplacementsReviewed(@RequestParam WikipediaLanguage lang) {
        return replacementService.countReplacementsReviewed(lang);
    }

    @GetMapping(value = "/count", params = "reviewed=false")
    public Long countReplacementsNotReviewed(@RequestParam WikipediaLanguage lang) {
        return replacementService.countReplacementsNotReviewed(lang);
    }

    @GetMapping(value = "/count", params = { "reviewed=true", "grouped" })
    public List<ReviewerCount> countReplacementsGroupedByReviewer(@RequestParam WikipediaLanguage lang) {
        return replacementService.countReplacementsGroupedByReviewer(lang);
    }

    @GetMapping(value = "/count", params = { "reviewed=false", "grouped" })
    public List<TypeCount> countReplacementsGroupedByType(@RequestParam WikipediaLanguage lang)
        throws ReplacerException {
        return replacementService.countReplacementsGroupedByType(lang);
    }
}
