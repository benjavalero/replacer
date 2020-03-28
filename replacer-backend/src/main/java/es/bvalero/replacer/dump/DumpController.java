package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions to start the dump processing or find the current status.
 */
@Slf4j
@RestController
@RequestMapping("api/dump")
public class DumpController {
    @Autowired
    private DumpManager dumpManager;

    @GetMapping(value = "")
    public DumpIndexation getDumpStatus() {
        return dumpManager.getDumpIndexation();
    }

    @PostMapping(value = "")
    public void processLatestDumpFileManually() {
        dumpManager.processLatestDumpFile();
    }
}
