package es.bvalero.replacer.dump;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.authentication.userrights.CheckUserRightsService;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.exception.ForbiddenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform actions related to the dump indexing process */
@Tag(name = "Dump Indexing")
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @Autowired
    private CheckUserRightsService checkUserRightsService;

    @Operation(summary = "Find the status of the current (or the last) dump indexing")
    @Loggable(skipResult = true)
    @GetMapping(value = "")
    public DumpIndexingStatus getDumpIndexingStatus(@Valid CommonQueryParameters queryParameters)
        throws ForbiddenException {
        checkUserRightsService.validateAdminUser(queryParameters.getUser());
        return dumpManager.getDumpIndexingStatus();
    }

    @Operation(summary = "Start manually a dump indexing")
    @Loggable(entered = true, skipResult = true)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "")
    public void manualStartDumpIndexing(@Valid CommonQueryParameters queryParameters) throws ForbiddenException {
        checkUserRightsService.validateAdminUser(queryParameters.getUser());
        dumpManager.indexLatestDumpFiles();
    }
}
