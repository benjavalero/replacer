package es.bvalero.replacer.page.list;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pages")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api/pages")
public class PageListController {

    @Autowired
    private PageUnreviewedTitleListService pageUnreviewedTitleListService;

    @Autowired
    private ReviewByTypeService reviewByTypeService;

    @Operation(
        summary = "Produce a list in plain text with the titles of the pages containing the given replacement type to review"
    )
    @GetMapping(value = "", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPageTitlesToReviewByType(
        @Valid CommonQueryParameters queryParameters,
        @Valid PageListRequest request
    ) {
        String titleList = StringUtils.join(
            pageUnreviewedTitleListService.findPageTitlesToReviewByType(
                queryParameters.getWikipediaLanguage(),
                request.getType(),
                request.getSubtype()
            ),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @Operation(summary = "Mark as reviewed by the system all pages pages containing the given replacement type")
    @PostMapping(value = "/review")
    public void reviewAsSystemByType(@Valid CommonQueryParameters queryParameters, @Valid PageListRequest request) {
        // Set as reviewed in the database
        reviewByTypeService.reviewAsSystemByType(
            queryParameters.getWikipediaLanguage(),
            request.getType(),
            request.getSubtype()
        );
    }
}
