package es.bvalero.replacer.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions to start the dump processing or find the current status.
 */
@RestController
@RequestMapping("dump")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @GetMapping(value = "/status")
    public DumpProcessStatus getDumpStatus() {
        return dumpManager.getDumpStatus();
    }

    /*
    @GetMapping(value = "/run")
    public boolean processLatestDumpFileManually() {
        // TODO : Estos métodos deberían devolver el true lo antes posible
        dumpManager.processLatestDumpFile(false);
        return true;
    }

    @GetMapping(value = "/run/force")
    public boolean processLatestDumpFileManuallyForced() {
        dumpManager.processLatestDumpFile(true);
        return true;
    }
*/
}
