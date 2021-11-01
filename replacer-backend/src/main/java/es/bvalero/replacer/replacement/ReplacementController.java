package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.domain.ReplacerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "replacements")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api")
public class ReplacementController {

    @Autowired
    private ReplacementService replacementService;

    @ApiOperation(value = "Count the number replacements")
    @GetMapping(value = "/replacements/count")
    public ReplacementCount countReplacementsReviewed(
        @ApiParam(value = "Filter by reviewed/unreviewed replacements", required = true) @RequestParam boolean reviewed,
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang
    ) {
        return ReplacementCount.of(
            reviewed
                ? replacementService.countReplacementsReviewed(lang)
                : replacementService.countReplacementsNotReviewed(lang)
        );
    }

    @ApiOperation(value = "List users with the number of reviewed replacements")
    @GetMapping(value = "/users/count")
    public List<ReviewerCount> countReplacementsGroupedByReviewer(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang
    ) {
        return replacementService.countReplacementsGroupedByReviewer(lang);
    }

    @ApiOperation(
        value = "List replacement types with the number of pages containing replacement of these types to review"
    )
    @GetMapping(value = "/replacement-types/count")
    public List<TypeCount> countReplacementsGroupedByType(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang
    ) throws ReplacerException {
        return replacementService.countReplacementsGroupedByType(lang);
    }
}
