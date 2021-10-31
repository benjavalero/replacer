package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.UserParameters;
import es.bvalero.replacer.wikipedia.WikipediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions related to the dump indexing process.
 */
@Slf4j
@Api(tags = "dump-indexing")
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @Autowired
    private WikipediaService wikipediaService;

    @ApiOperation(value = "Find the status of the current (or the last) dump indexation")
    @Loggable(value = Loggable.DEBUG, trim = false)
    @GetMapping(value = "")
    public ResponseEntity<DumpIndexingStatus> getDumpIndexingStatus(UserParameters params) {
        if (wikipediaService.isAdminUser(params.getUser())) {
            return new ResponseEntity<>(dumpManager.getDumpIndexingStatus(), HttpStatus.OK);
        } else {
            LOGGER.error("Unauthorized user: {}", params.getUser());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @ApiOperation(value = "Start manually a dump indexation")
    @Loggable(prepend = true)
    @PostMapping(value = "")
    public ResponseEntity<String> manualStartDumpIndexing(UserParameters params) {
        if (wikipediaService.isAdminUser(params.getUser())) {
            dumpManager.processLatestDumpFiles();
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            LOGGER.error("Unauthorized user: {}", params.getUser());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
