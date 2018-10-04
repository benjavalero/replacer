package es.bvalero.replacer.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions to start the dump processing or find the current status.
 */
@RestController
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @RequestMapping("/dump/status")
    public DumpProcessStatus getDumpStatus() {
        return dumpManager.getProcessStatus();
    }

    @RequestMapping("/dump/run")
    public boolean processLatestDumpFileManually() {
        dumpManager.processLatestDumpFile(true, false);
        return true;
    }

    @RequestMapping("/dump/run/force")
    public boolean processLatestDumpFileManuallyForced() {
        dumpManager.processLatestDumpFile(true, true);
        return true;
    }

}
