package es.bvalero.replacer.replacement;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller to get different counts of replacements */
@Tag(name = "Replacements")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api/replacement")
public class ReplacementCountController {

    @Autowired
    private ReplacementCountService replacementCountService;

    @Operation(summary = "Count the number of reviewed/unreviewed replacements including the custom ones")
    @GetMapping(value = "/count")
    public ReplacementCount countReplacements(
        @Valid CommonQueryParameters queryParameters,
        @Parameter(
            description = "Filter by reviewed/unreviewed replacements",
            required = true,
            example = "true"
        ) @RequestParam boolean reviewed
    ) {
        WikipediaLanguage lang = queryParameters.getLang().toDomain();
        return reviewed
            ? ReplacementCount.of(replacementCountService.countReplacementsReviewed(lang))
            : ReplacementCount.of(replacementCountService.countReplacementsNotReviewed(lang));
    }

    @Operation(summary = "Count the number of reviewed replacements grouped by reviewer in descending order by count")
    @GetMapping(value = "/user/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(@Valid CommonQueryParameters queryParameters) {
        return replacementCountService
            .countReplacementsGroupedByReviewer(queryParameters.getLang().toDomain())
            .stream()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Operation(
        summary = "Count the number of replacements to review, including the custom ones, grouped by page in descending order by count"
    )
    @ValidateAdminUser
    @GetMapping(value = "/page/count")
    public Collection<PageCount> countNotReviewedGroupedByPage(@Valid CommonQueryParameters queryParameters) {
        return replacementCountService
            .countNotReviewedGroupedByPage(queryParameters.getLang().toDomain())
            .stream()
            .map(count ->
                PageCount.of(count.getKey().getPageKey().getPageId(), count.getKey().getTitle(), count.getCount())
            )
            .collect(Collectors.toUnmodifiableList());
    }
}
