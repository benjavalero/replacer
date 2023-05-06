package es.bvalero.replacer.replacement;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.UserLanguage;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST controller to get different counts of replacements */
@Tag(name = "Replacement")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api/replacement")
public class ReplacementCountController {

    @Autowired
    private ReplacementCountService replacementCountService;

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
        return reviewed
            ? ReplacementCount.of(replacementCountService.countReplacementsReviewed(lang))
            : ReplacementCount.of(replacementCountService.countReplacementsNotReviewed(lang));
    }

    @Operation(summary = "Count the number of reviewed replacements grouped by reviewer in descending order by count")
    @GetMapping(value = "/user/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(@UserLanguage WikipediaLanguage lang) {
        return replacementCountService
            .countReplacementsGroupedByReviewer(lang)
            .stream()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Operation(
        summary = "Count the number of replacements to review, including the custom ones, grouped by page in descending order by count"
    )
    @ValidateAdminUser
    @GetMapping(value = "/page/count")
    public Collection<PageCount> countNotReviewedGroupedByPage(@AuthenticatedUser User user) {
        return replacementCountService
            .countNotReviewedGroupedByPage(user.getId().getLang())
            .stream()
            .map(count ->
                PageCount.of(count.getKey().getPageKey().getPageId(), count.getKey().getTitle(), count.getCount())
            )
            .collect(Collectors.toUnmodifiableList());
    }
}
