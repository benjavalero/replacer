package es.bvalero.replacer.dump;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

/** The status of the current (or the last) dump indexing */
@Value
@Builder
class DumpStatus {

    boolean running;

    @Nullable
    Integer numPagesRead;

    @Nullable
    Integer numPagesIndexed;

    @Nullable
    Integer numPagesEstimated;

    @Nullable
    String dumpFileName;

    @Nullable
    LocalDateTime start;

    @Nullable
    LocalDateTime end;

    static DumpStatus ofEmpty() {
        return DumpStatus.builder().running(false).build();
    }
}
