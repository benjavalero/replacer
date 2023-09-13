package es.bvalero.replacer.page.review;

import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@RestController
@RequestMapping("api/page")
public class ReviewSaveController {

    @Autowired
    private ReviewSaveService reviewSaveService;

    @Operation(summary = "Save a review: update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<Void> saveReview(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @AuthenticatedUser User user,
        @Valid @RequestBody SaveReviewRequest request
    ) throws WikipediaException {
        LOGGER.info("POST Save Review: {}", request);

        String content = request.getContent();
        if (StringUtils.isBlank(content) && !request.isReviewedWithoutChanges()) {
            LOGGER.error("Non valid empty content");
            return ResponseEntity.badRequest().build();
        }
        Collection<ReviewedReplacement> reviewed = ReviewMapper.fromDto(
            pageId,
            request.getReviewedReplacements(),
            request.getSectionOffset(),
            user
        );
        if (request.isReviewedWithoutChanges()) {
            reviewSaveService.markAsReviewed(reviewed, false);
        } else {
            PageKey pageKey = PageKey.of(user.getId().getLang(), pageId);
            WikipediaTimestamp saveTimestamp = WikipediaTimestamp.of(request.getQueryTimestamp());
            WikipediaPage page = WikipediaPage
                .builder()
                .pageKey(pageKey)
                .namespace(WikipediaNamespace.ARTICLE) // TODO: Not relevant for saving
                .title("") // TODO: Not relevant for saving
                .content(request.getContent())
                .lastUpdate(saveTimestamp)
                .queryTimestamp(saveTimestamp)
                .build();
            reviewSaveService.saveReviewContent(page, request.getSectionId(), reviewed, user.getAccessToken());
            reviewSaveService.markAsReviewed(reviewed, true);
        }

        return ResponseEntity.noContent().build();
    }
}
