package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.UserLanguage;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/** REST controller to get different counts of replacements */
@Tag(name = "Replacement")
@Slf4j
@RestController
@RequestMapping("api/replacement")
public class ReplacementCountController {

    // Dependency injection
    private final ReplacementCountService replacementCountService;

    public ReplacementCountController(ReplacementCountService replacementCountService) {
        this.replacementCountService = replacementCountService;
    }

    @Operation(summary = "Count the number of reviewed/unreviewed replacements including the custom ones")
    @GetMapping(value = "/count")
    public ReplacementCount countReplacements(
        @UserLanguage WikipediaLanguage lang,
        @Parameter(
            description = "Filter by reviewed/unreviewed replacements",
            required = true,
            example = "true"
        ) @RequestParam boolean reviewed
    ) {
        ReplacementCount count;
        if (reviewed) {
            count = ReplacementCount.of(replacementCountService.countReplacementsReviewed(lang));
            LOGGER.info("GET Count Reviewed Replacements: {}", count);
        } else {
            count = ReplacementCount.of(replacementCountService.countReplacementsNotReviewed(lang));
            LOGGER.info("GET Count Not Reviewed Replacements: {}", count);
        }
        return count;
    }

    @Operation(summary = "Count the number of reviewed replacements grouped by reviewer in descending order by count")
    @GetMapping(value = "/user/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(@UserLanguage WikipediaLanguage lang) {
        Collection<ReviewerCount> counts = replacementCountService
            .countReplacementsGroupedByReviewer(lang)
            .stream()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .toList();
        LOGGER.info("GET Count Replacements By Reviewer: {} items", counts.size());
        return counts;
    }

    @Operation(
        summary = "Count the number of replacements to review, including the custom ones, grouped by page in descending order by count"
    )
    @ValidateAdminUser
    @GetMapping(value = "/page/count")
    public Collection<PageCount> countReplacementsNotReviewedGroupedByPage(@UserLanguage WikipediaLanguage lang) {
        Collection<PageCount> counts = replacementCountService
            .countReplacementsNotReviewedGroupedByPage(lang)
            .stream()
            .map(count ->
                PageCount.of(count.getKey().getPageKey().getPageId(), count.getKey().getTitle(), count.getCount())
            )
            .toList();
        LOGGER.info("GET Count Replacements Not Reviewed By Page: {} items", counts.size());
        return counts;
    }
}
