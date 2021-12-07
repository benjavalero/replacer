package es.bvalero.replacer.page.save;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.review.PageReviewMapper;
import es.bvalero.replacer.page.review.PageReviewOptions;
import es.bvalero.replacer.page.review.ReviewSection;
import es.bvalero.replacer.wikipedia.WikipediaDateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "pages")
@Loggable(prepend = true, trim = false)
@RestController
@RequestMapping("api/pages")
public class PageSaveController {

    static final String EMPTY_CONTENT = " ";

    @Autowired
    private PageSaveService pageSaveService;

    @ApiOperation(value = "Update page contents and mark as reviewed")
    @PostMapping(value = "/{id}")
    public ResponseEntity<String> save(
        @PathVariable("id") int pageId,
        @ApiParam(value = "Language", required = true) @RequestParam WikipediaLanguage lang,
        @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero") @RequestParam String user,
        @Valid @RequestBody PageSaveRequest request
    ) throws ReplacerException {
        if (!Objects.equals(pageId, request.getPage().getId())) {
            return new ResponseEntity<>("Page ID mismatch", HttpStatus.BAD_REQUEST);
        }
        if (!Objects.equals(lang, request.getPage().getLang())) {
            return new ResponseEntity<>("Language mismatch", HttpStatus.BAD_REQUEST);
        }

        String content = request.getPage().getContent();
        if (StringUtils.isBlank(content) && !EMPTY_CONTENT.equals(content)) {
            return new ResponseEntity<>("Non valid empty content", HttpStatus.BAD_REQUEST);
        }
        PageReviewOptions options = PageReviewMapper.fromDto(request.getSearch(), lang, user);
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
                .id(WikipediaPageId.of(lang, pageId))
                .namespace(WikipediaNamespace.getDefault()) // Not relevant for saving
                .title(request.getPage().getTitle())
                .content(request.getPage().getContent())
                .lastUpdate(saveTimestamp)
                .queryTimestamp(saveTimestamp)
                .build();
            AccessToken accessToken = AccessToken.of(request.getToken(), request.getTokenSecret());
            pageSaveService.savePageContent(page, sectionId, options, accessToken);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}