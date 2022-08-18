package es.bvalero.replacer.replacement.count;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ResultCount;
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

@Tag(name = "Replacements")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api/replacement")
public class ReplacementCountController {

    @Autowired
    private ReplacementCountService replacementCountService;

    @Operation(summary = "Count the reviewed or unreviewed replacements")
    @GetMapping(value = "/count")
    public ReplacementCount countReplacementsReviewed(
        @Valid CommonQueryParameters queryParameters,
        @Parameter(
            description = "Filter by reviewed/unreviewed replacements",
            required = true,
            example = "true"
        ) @RequestParam boolean reviewed
    ) {
        WikipediaLanguage lang = queryParameters.getWikipediaLanguage();
        return ReplacementCount.of(
            reviewed
                ? replacementCountService.countReplacementsReviewed(lang)
                : replacementCountService.countReplacementsNotReviewed(lang)
        );
    }

    @Operation(summary = "Count the reviewed replacements grouped by reviewer user")
    @GetMapping(value = "/user/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(@Valid CommonQueryParameters queryParameters) {
        return toReviewerCountDto(
            replacementCountService.countReplacementsGroupedByReviewer(queryParameters.getWikipediaLanguage())
        );
    }

    private Collection<ReviewerCount> toReviewerCountDto(Collection<ResultCount<String>> counts) {
        return counts
            .stream()
            .sorted()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Operation(summary = "Count the replacements to review grouped by page")
    @ValidateAdminUser
    @GetMapping(value = "/page/count")
    public Collection<PageCount> countReplacementsGroupedByPage(@Valid CommonQueryParameters queryParameters) {
        return toPageCountDto(
            replacementCountService.countReplacementsGroupedByPage(queryParameters.getWikipediaLanguage())
        );
    }

    private Collection<PageCount> toPageCountDto(Collection<ResultCount<PageModel>> counts) {
        return counts
            .stream()
            .map(count -> PageCount.of(count.getKey().getPageId(), count.getKey().getTitle(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Operation(summary = "Count the pages to review grouped by type (kind-subtype)")
    @GetMapping(value = "/type/count")
    public Collection<KindCount> countReplacementsGroupedByType(@Valid CommonQueryParameters queryParameters) {
        return replacementCountService.countReplacementsGroupedByType(queryParameters.getWikipediaLanguage());
    }
}
