package es.bvalero.replacer.replacement.stats;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.repository.ResultCount;
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
@RequestMapping("api")
public class ReplacementStatsController {

    @Autowired
    private ReplacementStatsService replacementStatsService;

    @Operation(summary = "Count the reviewed or unreviewed replacements")
    @GetMapping(value = "/replacements/count")
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
                ? replacementStatsService.countReplacementsReviewed(lang)
                : replacementStatsService.countReplacementsNotReviewed(lang)
        );
    }

    @Operation(summary = "Number of reviewed replacements grouped by reviewer")
    @GetMapping(value = "/users/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(@Valid CommonQueryParameters queryParameters) {
        return toDto(
            replacementStatsService.countReplacementsGroupedByReviewer(queryParameters.getWikipediaLanguage())
        );
    }

    private Collection<ReviewerCount> toDto(Collection<ResultCount<String>> counts) {
        return counts
            .stream()
            .sorted()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }
}
