package es.bvalero.replacer.page.save;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.page.review.PageReviewMapper;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.ReviewSection;
import es.bvalero.replacer.wikipedia.WikipediaConflictException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pages")
@Slf4j
@RestController
@RequestMapping("api/pages")
public class PageSaveController {

    static final String EMPTY_CONTENT = " ";

    @Autowired
    private PageSaveService pageSaveService;

    @Loggable(skipResult = true)
    @Operation(summary = "Update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<Void> save(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid @RequestBody PageSaveRequest request
    ) {
        if (!Objects.equals(pageId, request.getPage().getId())) {
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
        PageReviewOptions options = PageReviewMapper.fromDto(
            request.getOptions(),
            request.isReviewAllTypes(),
            queryParameters
        );
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(queryParameters.getWikipediaLanguage(), pageId);
        if (EMPTY_CONTENT.equals(content)) {
            pageSaveService.savePageWithNoChanges(wikipediaPageId, options);
        } else {
            ReviewSection section = request.getPage().getSection();
            Integer sectionId = section == null ? null : section.getId();
            LocalDateTime saveTimestamp = WikipediaDateUtils.parseWikipediaTimestamp(
                request.getPage().getQueryTimestamp()
            );
            WikipediaPage page = WikipediaPage
                .builder()
                .id(WikipediaPageId.of(queryParameters.getWikipediaLanguage(), pageId))
                .namespace(WikipediaNamespace.getDefault()) // Not relevant for saving
                .title(request.getPage().getTitle())
                .content(request.getPage().getContent())
                .lastUpdate(saveTimestamp)
                .queryTimestamp(saveTimestamp)
                .build();
            AccessToken accessToken = AccessTokenDto.toDomain(request.getAccessToken());
            try {
                pageSaveService.savePageContent(page, sectionId, options, accessToken);
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
