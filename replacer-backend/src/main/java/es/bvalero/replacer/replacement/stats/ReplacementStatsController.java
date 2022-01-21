package es.bvalero.replacer.replacement.stats;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.repository.ResultCount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        return mergeDuplicates(counts)
            .stream()
            .sorted()
            .map(count -> ReviewerCount.of(count.getKey(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }

    private Collection<ResultCount<String>> mergeDuplicates(Collection<ResultCount<String>> counts) {
        List<ResultCount<String>> checked = new ArrayList<>(counts.size());

        for (ResultCount<String> current : counts) {
            // Search in the previous ones if there is any item to be merged to
            boolean duplicateFound = false;
            for (int j = 0; j < checked.size(); j++) {
                ResultCount<String> previous = checked.get(j);
                if (current.getKey().equals(previous.getKey())) {
                    checked.set(j, previous.merge(current));
                    duplicateFound = true;
                }
            }
            if (!duplicateFound) {
                checked.add(current);
            }
        }

        return checked;
    }
}
