package es.bvalero.replacer.dump;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.user.ValidateAdminUser;
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

    @Operation(summary = "Find the status of the current (or the last) dump indexing")
    @Loggable(skipResult = true)
    @ValidateAdminUser
    @GetMapping(value = "")
    public DumpIndexingStatusResponse getDumpIndexingStatus(@Valid CommonQueryParameters queryParameters) {
        return toDto(dumpManager.getDumpIndexingStatus());
    }

    @Operation(summary = "Start manually a dump indexing")
    @Loggable(entered = true, skipResult = true)
    @ValidateAdminUser
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "")
    public void manualStartDumpIndexing(@Valid CommonQueryParameters queryParameters) {
        dumpManager.indexLatestDumpFiles();
    }

    private DumpIndexingStatusResponse toDto(DumpIndexingStatus status) {
        return DumpIndexingStatusResponse
            .builder()
            .running(status.isRunning())
            .numPagesRead(status.getNumPagesRead())
            .numPagesIndexed(status.getNumPagesIndexed())
            .numPagesEstimated(status.getNumPagesEstimated())
            .dumpFileName(status.getDumpFileName())
            .start(ReplacerUtils.convertLocalDateTimeToMilliseconds(status.getStart()))
            .end(ReplacerUtils.convertLocalDateTimeToMilliseconds(status.getEnd()))
            .build();
    }
}