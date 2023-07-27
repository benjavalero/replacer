package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform actions related to the dump indexing process */
@Tag(name = "Dump Indexing")
@Slf4j
@RestController
@RequestMapping("api/dump-indexing")
public class DumpController {

    @Autowired
    private DumpManager dumpManager;

    @Operation(summary = "Find the status of the current (or the last) dump indexing")
    @ValidateAdminUser
    @GetMapping(value = "")
    public DumpIndexingStatusDto getDumpIndexingStatus() {
        DumpIndexingStatusDto dto = toDto(dumpManager.getDumpIndexingStatus());
        LOGGER.debug("GET Dump Indexing Status: {}", dto);
        return dto;
    }

    @Operation(summary = "Start manually a dump indexing")
    @ValidateAdminUser
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "")
    public void manualStartDumpIndexing() {
        LOGGER.info("START Manual Dump Indexing...");
        dumpManager.indexLatestDumpFiles();
    }

    private DumpIndexingStatusDto toDto(DumpIndexingStatus status) {
        return DumpIndexingStatusDto
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
