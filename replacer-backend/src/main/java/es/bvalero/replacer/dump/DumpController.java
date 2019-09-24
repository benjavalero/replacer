package es.bvalero.replacer.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions to start the dump processing or find the current status.
 */
@RestController
@RequestMapping("api/dump")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @GetMapping(value = "/")
    public DumpIndexation getDumpStatus() {
        return dumpManager.getDumpStatus();
    }

    @PostMapping(value = "/")
    public void processLatestDumpFileManually() {
        dumpManager.processLatestDumpFile(false);
    }

    @PostMapping(value = "/force")
    public void processLatestDumpFileManuallyForced() {
        dumpManager.processLatestDumpFile(true);
    }

}
