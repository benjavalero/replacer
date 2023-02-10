package es.bvalero.replacer.review;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.AccessTokenDto;
import es.bvalero.replacer.wikipedia.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review")
@Slf4j
@RestController
@RequestMapping("api/review")
public class ReviewSaveController {

    static final String EMPTY_CONTENT = " ";

    @Autowired
    private ReviewSaveService reviewSaveService;

    @Loggable(skipResult = true)
    @Operation(summary = "Save a review: update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<Void> saveReview(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid @RequestBody SaveReviewRequest request
    ) {
        if (!Objects.equals(pageId, request.getPage().getPageId())) {
            LOGGER.error("Page ID mismatch");
            return ResponseEntity.badRequest().build();
        }
        if (!Objects.equals(queryParameters.getLang(), request.getPage().getLang())) {
            LOGGER.error("Language mismatch");
            return ResponseEntity.badRequest().build();
        }

        String content = request.getPage().getContent();
        if (StringUtils.isBlank(content) && !EMPTY_CONTENT.equals(content)) {
            LOGGER.error("Non valid empty content");
            return ResponseEntity.badRequest().build();
        }
        Collection<ReviewedReplacement> reviewed = ReviewMapper.fromDto(
            pageId,
            request.getReviewedReplacements(),
            request.getPage().getSectionOffset(),
            queryParameters
        );
        if (EMPTY_CONTENT.equals(content)) {
            reviewSaveService.markAsReviewed(reviewed, false);
        } else {
            PageKey pageKey = PageKey.of(queryParameters.getWikipediaLanguage(), pageId);
            ReviewSectionDto section = request.getPage().getSection();
            Integer sectionId = section == null ? null : section.getId();
            WikipediaTimestamp saveTimestamp = WikipediaTimestamp.of(request.getPage().getQueryTimestamp());
            WikipediaPage page = WikipediaPage
                .builder()
                .pageKey(pageKey)
                .namespace(WikipediaNamespace.ARTICLE) // TODO: Not relevant for saving
                .title(request.getPage().getTitle())
                .content(request.getPage().getContent())
                .lastUpdate(saveTimestamp)
                .queryTimestamp(saveTimestamp)
                .build();
            AccessToken accessToken = AccessTokenDto.toDomain(request.getAccessToken());
            try {
                reviewSaveService.saveReviewContent(page, sectionId, reviewed, accessToken);
                reviewSaveService.markAsReviewed(reviewed, true);
            } catch (WikipediaException e) {
                return manageWikipediaException(e);
            }
        }

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> manageWikipediaException(WikipediaException e) {
        if (e instanceof WikipediaConflictException) {
            LOGGER.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else if (e.getMessage() != null && e.getMessage().contains("mwoauth-invalid-authorization")) {
            LOGGER.warn("Authentication error saving page content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            LOGGER.error("Error saving page content", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
