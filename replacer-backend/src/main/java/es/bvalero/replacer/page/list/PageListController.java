package es.bvalero.replacer.page.list;

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "pages")
@Loggable(prepend = true, trim = false)
@RestController
@RequestMapping("api/pages")
public class PageListController {

    @Autowired
    private PageListService pageListService;

    @ApiOperation(
        value = "Produce a list in plain text with the titles of the pages containing the given replacement type to review"
    )
    @GetMapping(value = "", params = { "type", "subtype" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPageTitlesToReviewByType(@Valid PageListRequest request) {
        String titleList = StringUtils.join(
            pageListService.findPageTitlesToReviewByType(request.getLang(), request.getType(), request.getSubtype()),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @ApiOperation(value = "Mark as reviewed by the system all pages pages containing the given replacement type")
    @PostMapping(value = "/review", params = { "type", "subtype" })
    public void reviewAsSystemByType(@Valid PageListRequest request) {
        // Set as reviewed in the database
        pageListService.reviewAsSystemByType(request.getLang(), request.getType(), request.getSubtype());
    }
}
