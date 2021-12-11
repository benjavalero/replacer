package es.bvalero.replacer.replacement.stats;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
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

    @Operation(summary = "Count the number replacements")
    @GetMapping(value = "/replacements/count")
    public ReplacementCount countReplacementsReviewed(
        @Valid CommonQueryParameters queryParameters,
        @Parameter(
            description = "Filter by reviewed/unreviewed replacements",
            required = true,
            example = "true"
        ) @RequestParam boolean reviewed
    ) {
        WikipediaLanguage lang = queryParameters.getLang();
        return ReplacementCount.of(
            reviewed
                ? replacementStatsService.countReplacementsReviewed(lang)
                : replacementStatsService.countReplacementsNotReviewed(lang)
        );
    }

    @Operation(summary = "List users with the number of reviewed replacements")
    @GetMapping(value = "/users/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(@Valid CommonQueryParameters queryParameters) {
        return replacementStatsService.countReplacementsGroupedByReviewer(queryParameters.getLang());
    }
}
