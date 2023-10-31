package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.user.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller to perform actions related to the dump indexing process */
@Tag(name = "Dump")
@Slf4j
@RestController
@RequestMapping("api/dump")
public class DumpController {

    // Dependency injection
    private final DumpManager dumpManager;

    public DumpController(DumpManager dumpManager) {
        this.dumpManager = dumpManager;
    }

    @Operation(summary = "Find the status of the current (or the last) dump indexing")
    @ValidateAdminUser
    @GetMapping(value = "")
    public ResponseEntity<DumpStatusDto> getDumpStatus() {
        Optional<DumpStatus> dumpStatus = dumpManager.getDumpStatus();
        if (dumpStatus.isPresent()) {
            DumpStatusDto dto = toDto(dumpStatus.get());
            LOGGER.debug("GET Dump Indexing Status: {}", dto);
            return ResponseEntity.ok(dto);
        } else {
            LOGGER.debug("GET Dump Indexing Status: EMPTY");
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Start manually a dump indexing")
    @ValidateAdminUser
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "")
    public void manualStartDumpIndexing() {
        LOGGER.info("START Manual Dump Indexing...");
        dumpManager.indexLatestDumpFiles();
    }

    private DumpStatusDto toDto(DumpStatus status) {
        return DumpStatusDto
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
