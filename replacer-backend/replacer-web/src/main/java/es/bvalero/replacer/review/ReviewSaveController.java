package es.bvalero.replacer.review;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.AccessTokenDto;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review")
@Slf4j
@RestController
@RequestMapping("api/review")
public class ReviewSaveController {

    @Autowired
    private ReviewSaveService reviewSaveService;

    @Loggable(skipResult = true)
    @Operation(summary = "Save a review: update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<Void> saveReview(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid @RequestBody SaveReviewRequest request
    ) throws WikipediaException {
        if (!Objects.equals(pageId, request.getPage().getPageId())) {
            LOGGER.error("Page ID mismatch");
            return ResponseEntity.badRequest().build();
        }
        if (!Objects.equals(queryParameters.getWikipediaLanguage().getCode(), request.getPage().getLang())) {
            LOGGER.error("Language mismatch");
            return ResponseEntity.badRequest().build();
        }

        String content = request.getPage().getContent();
        if (StringUtils.isBlank(content) && !request.getPage().isReviewedWithoutChanges()) {
            LOGGER.error("Non valid empty content");
            return ResponseEntity.badRequest().build();
        }
        Collection<ReviewedReplacement> reviewed = ReviewMapper.fromDto(
            pageId,
            request.getReviewedReplacements(),
            request.getPage().getSectionOffset(),
            queryParameters
        );
        if (request.getPage().isReviewedWithoutChanges()) {
            reviewSaveService.markAsReviewed(reviewed, false);
        } else {
            PageKey pageKey = PageKey.of(queryParameters.getWikipediaLanguage(), pageId);
            ReviewSection section = request.getPage().getSection();
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
            reviewSaveService.saveReviewContent(page, sectionId, reviewed, accessToken);
            reviewSaveService.markAsReviewed(reviewed, true);
        }

        return ResponseEntity.noContent().build();
    }
}
