package es.bvalero.replacer.replacement.stats;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "replacements")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api")
public class ReplacementStatsController {

    @Autowired
    private ReplacementStatsService replacementStatsService;

    @ApiOperation(value = "Count the number replacements")
    @GetMapping(value = "/replacements/count")
    public ReplacementCount countReplacementsReviewed(
        @ApiParam(value = "Filter by reviewed/unreviewed replacements", required = true) @RequestParam boolean reviewed,
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang
    ) {
        return ReplacementCount.of(
            reviewed
                ? replacementStatsService.countReplacementsReviewed(lang)
                : replacementStatsService.countReplacementsNotReviewed(lang)
        );
    }

    @ApiOperation(value = "List users with the number of reviewed replacements")
    @GetMapping(value = "/users/count")
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang
    ) {
        return replacementStatsService.countReplacementsGroupedByReviewer(lang);
    }
}
