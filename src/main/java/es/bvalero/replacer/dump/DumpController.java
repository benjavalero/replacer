package es.bvalero.replacer.dump;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @RequestMapping(value = "/dump/status")
    DumpStatus getDumpStatus() {
        return dumpManager.getDumpStatus();
    }

    @RequestMapping(value = "/dump/run")
    boolean runIndexation() {
        dumpManager.runIndexation();
        return true;
    }

}
