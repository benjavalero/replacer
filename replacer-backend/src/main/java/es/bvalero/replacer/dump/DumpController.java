package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.exception.UnauthorizedException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform actions related to the dump indexing process */
@Slf4j
@Api(tags = "dump-indexing")
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @Autowired
    private WikipediaService wikipediaService;

    @ApiOperation(value = "Find the status of the current (or the last) dump indexing")
    @Loggable(value = Loggable.DEBUG, trim = false)
    @GetMapping(value = "")
    public DumpIndexingStatus getDumpIndexingStatus(
        @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero") @RequestParam String user
    ) throws UnauthorizedException {
        validateAdminUser(user);
        return dumpManager.getDumpIndexingStatus();
    }

    @ApiOperation(value = "Start manually a dump indexing")
    @Loggable(prepend = true)
    @PostMapping(value = "")
    public void manualStartDumpIndexing(
        @ApiParam(value = "Wikipedia user name", required = true, example = "Benjavalero") @RequestParam String user
    ) throws UnauthorizedException {
        validateAdminUser(user);
        dumpManager.processLatestDumpFiles();
    }

    private void validateAdminUser(String user) throws UnauthorizedException {
        if (!wikipediaService.isAdminUser(user)) {
            LOGGER.error("Unauthorized user: {}", user);
            throw new UnauthorizedException();
        }
    }
}
