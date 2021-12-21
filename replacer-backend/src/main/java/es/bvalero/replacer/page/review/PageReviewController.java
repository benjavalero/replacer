package es.bvalero.replacer.page.review;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.PageReviewOptionsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pages")
@Loggable(entered = true)
@RestController
@RequestMapping("api/pages")
public class PageReviewController {

    @Autowired
    private PageReviewNoTypeFinder pageReviewNoTypeFinder;

    @Autowired
    private PageReviewTypeSubtypeFinder pageReviewTypeSubtypeFinder;

    @Autowired
    private PageReviewCustomFinder pageReviewCustomFinder;

    @Autowired
    private ReplacementValidationService replacementValidationService;

    @Operation(summary = "Find a random page and the replacements to review")
    @GetMapping(value = "/random")
    public Optional<PageReviewResponse> findRandomPageWithReplacements(
        @Valid CommonQueryParameters queryParameters,
        @Valid PageReviewOptionsDto optionsDto
    ) {
        Optional<PageReview> review = Optional.empty();
        PageReviewOptions options = PageReviewMapper.fromDto(optionsDto, queryParameters);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = pageReviewNoTypeFinder.findRandomPageReview(options);
                break;
            case TYPE_SUBTYPE:
                review = pageReviewTypeSubtypeFinder.findRandomPageReview(options);
                break;
            case CUSTOM:
                review = pageReviewCustomFinder.findRandomPageReview(options);
                break;
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }

    @Operation(summary = "Find a page and the replacements to review")
    @GetMapping(value = "/{id}")
    public Optional<PageReviewResponse> findPageReviewById(
        @Parameter(description = "Page ID", example = "6980716") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid PageReviewOptionsDto optionsDto
    ) {
        Optional<PageReview> review = Optional.empty();
        PageReviewOptions options = PageReviewMapper.fromDto(optionsDto, queryParameters);
        switch (options.getOptionsType()) {
            case NO_TYPE:
                review = pageReviewNoTypeFinder.getPageReview(pageId, options);
                break;
            case TYPE_SUBTYPE:
                review = pageReviewTypeSubtypeFinder.getPageReview(pageId, options);
                break;
            case CUSTOM:
                review = pageReviewCustomFinder.getPageReview(pageId, options);
                break;
        }
        return review.map(r -> PageReviewMapper.toDto(r, options));
    }

    @Operation(summary = "Validate if the custom replacement is a known type")
    @GetMapping(value = "/validate")
    public ResponseEntity<ReplacementValidationResponse> validateCustomReplacement(
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementValidationRequest validationRequest
    ) {
        Optional<ReplacementType> type = replacementValidationService.findMatchingReplacementType(
            queryParameters.getWikipediaLanguage(),
            validationRequest.getReplacement(),
            validationRequest.isCs()
        );
        if (type.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(ReplacementValidationResponse.of(type.get()));
        }
    }
}
