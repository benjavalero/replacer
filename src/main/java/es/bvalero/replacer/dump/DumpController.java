package es.bvalero.replacer.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @RequestMapping(value = "/dump/status")
    DumpStatus getDumpStatus() {
        return dumpManager.getProcessStatus();
    }

    @RequestMapping(value = "/dump/run")
    boolean processLatestDumpFileManually(@RequestParam("force") boolean forceProcessArticles) {
        dumpManager.processLatestDumpFile(true, forceProcessArticles);
        return true;
    }

}
