package es.bvalero.replacer.dump;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.authentication.UserAuthenticator;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.exception.UnauthorizedException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform actions related to the dump indexing process */
@Slf4j
@Api(tags = "dump-indexing")
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @Autowired
    private UserAuthenticator userAuthenticator;

    @ApiOperation(value = "Find the status of the current (or the last) dump indexing")
    @Loggable(skipResult = true)
    @GetMapping(value = "")
    public DumpIndexingStatus getDumpIndexingStatus(@Valid CommonQueryParameters queryParameters)
        throws UnauthorizedException {
        validateAdminUser(queryParameters.getUser());
        return dumpManager.getDumpIndexingStatus();
    }

    @ApiOperation(value = "Start manually a dump indexing")
    @Loggable(entered = true, skipResult = true)
    @PostMapping(value = "")
    public void manualStartDumpIndexing(@Valid CommonQueryParameters queryParameters) throws UnauthorizedException {
        validateAdminUser(queryParameters.getUser());
        dumpManager.indexLatestDumpFiles();
    }

    private void validateAdminUser(String user) throws UnauthorizedException {
        if (!userAuthenticator.isAdminUser(user)) {
            LOGGER.error("Unauthorized user: {}", user);
            throw new UnauthorizedException();
        }
    }
}
