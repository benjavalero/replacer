package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions related to the dump indexing process.
 */
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @Loggable(value = Loggable.DEBUG)
    @GetMapping(value = "/status")
    public DumpIndexingStatus getDumpIndexingStatus() {
        return dumpManager.getDumpIndexingStatus();
    }

    @Loggable(prepend = true)
    @PostMapping(value = "/start")
    public void manualStartDumpIndexing() {
        dumpManager.processLatestDumpFiles();
    }
}
