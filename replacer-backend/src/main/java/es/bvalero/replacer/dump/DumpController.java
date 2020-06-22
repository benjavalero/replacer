package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions related to the dump indexing process.
 */
@Slf4j
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {
    @Autowired
    private DumpManager dumpManager;

    @GetMapping(value = "/status")
    public DumpIndexingStatus getDumpIndexingStatus() {
        return dumpManager.getDumpIndexingStatus();
    }

    @PostMapping(value = "/start")
    public void postStart() {
        dumpManager.processLatestDumpFile();
    }
}
