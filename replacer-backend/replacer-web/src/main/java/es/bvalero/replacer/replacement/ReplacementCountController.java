package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.UserLanguage;
import es.bvalero.replacer.common.util.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.web.bind.annotation.*;

/** REST controller to get different counts of replacements */
@Tag(name = "Replacement")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/replacement")
class ReplacementCountController {

    // Dependency injection
    private final ReplacementCountService replacementCountService;

    ReplacementCountController(ReplacementCountService replacementCountService) {
        this.replacementCountService = replacementCountService;
    }

    @Operation(summary = "Count the number of reviewed/unreviewed replacements including the custom ones")
    @GetMapping(value = "/count")
    ReplacementCount countReplacements(
        @UserLanguage WikipediaLanguage lang,
        @Parameter(
            description = "Filter by reviewed/unreviewed replacements",
            required = true,
            example = "true"
        ) @RequestParam boolean reviewed
    ) {
        ReplacementCount count;
        if (reviewed) {
            count = ReplacementCount.of(replacementCountService.countReviewed(lang));
            LOGGER.info("GET Count Reviewed Replacements: {}", count);
        } else {
            count = ReplacementCount.of(replacementCountService.countNotReviewed(lang));
            LOGGER.info("GET Count Not Reviewed Replacements: {}", count);
        }
        return count;
    }

    @Operation(
        summary = "Count the number of reviewed replacements, including the custom ones, grouped by reviewer in descending order by count"
    )
    @GetMapping(value = "/user/count")
    Collection<ReviewerCount> countReviewedReplacementsGroupedByReviewer(@UserLanguage WikipediaLanguage lang) {
        Collection<ReviewerCount> counts = replacementCountService
            .countReviewedGroupedByReviewer(lang)
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
    Collection<PageCount> countNotReviewedReplacementsGroupedByPage(@UserLanguage WikipediaLanguage lang) {
        Collection<PageCount> counts = replacementCountService
            .countNotReviewedGroupedByPage(lang)
            .stream()
            .map(count ->
                PageCount.of(count.getKey().getPageKey().getPageId(), count.getKey().getTitle(), count.getCount())
            )
            .toList();
        LOGGER.info("GET Count Replacements Not Reviewed By Page: {} items", counts.size());
        return counts;
    }
}
