package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions related to the dump indexing process.
 */
@Api(tags = "dump-indexing")
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @ApiOperation(value = "Find the status of the current (or the last) dump indexation")
    @Loggable(value = Loggable.DEBUG, trim = false)
    @GetMapping(value = "")
    public DumpIndexingStatus getDumpIndexingStatus() {
        return dumpManager.getDumpIndexingStatus();
    }

    @ApiOperation(value = "Start manually a dump indexation")
    @Loggable(prepend = true)
    @PostMapping(value = "")
    public void manualStartDumpIndexing() {
        dumpManager.processLatestDumpFiles();
    }
}
