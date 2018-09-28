package es.bvalero.replacer.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @RequestMapping(value = "/dump/status")
    Map<String, Object> getDumpStatus() {
        return dumpManager.getProcessStatus();
    }

    @RequestMapping(value = "/dump/run")
    boolean processLatestDumpFileManually(@RequestParam("force") boolean forceProcessArticles) throws DumpException {
        dumpManager.processLatestDumpFile(true, forceProcessArticles);
        return true;
    }

}
