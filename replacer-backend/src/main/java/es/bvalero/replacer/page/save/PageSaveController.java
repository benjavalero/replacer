package es.bvalero.replacer.page.save;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    /* SAVE CHANGES */

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
        if (EMPTY_CONTENT.equals(content)) {
            pageSaveService.savePageWithNoChanges(user, request);
        } else {
            pageSaveService.savePageContent(user, request);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
