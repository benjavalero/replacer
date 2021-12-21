package es.bvalero.replacer.page.save;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.page.review.PageReviewMapper;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.ReviewSection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pages")
@RestController
@RequestMapping("api/pages")
public class PageSaveController {

    static final String EMPTY_CONTENT = " ";

    @Autowired
    private PageSaveService pageSaveService;

    @Loggable(skipResult = true)
    @Operation(summary = "Update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<String> save(
        @Parameter(description = "Page ID", example = "1") @PathVariable("id") int pageId,
        @Valid CommonQueryParameters queryParameters,
        @Valid @RequestBody PageSaveRequest request
    ) {
        if (!Objects.equals(pageId, request.getPage().getId())) {
            return new ResponseEntity<>("Page ID mismatch", HttpStatus.BAD_REQUEST);
        }
        if (!Objects.equals(queryParameters.getLang(), request.getPage().getLang())) {
            return new ResponseEntity<>("Language mismatch", HttpStatus.BAD_REQUEST);
        }

        String content = request.getPage().getContent();
        if (StringUtils.isBlank(content) && !EMPTY_CONTENT.equals(content)) {
            return new ResponseEntity<>("Non valid empty content", HttpStatus.BAD_REQUEST);
        }
        PageReviewOptions options = PageReviewMapper.fromDto(request.getSearch(), queryParameters);
        if (EMPTY_CONTENT.equals(content)) {
            pageSaveService.savePageWithNoChanges(pageId, options);
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
            pageSaveService.savePageContent(page, sectionId, options, accessToken);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
