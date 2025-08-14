package es.bvalero.replacer.dump;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** The status of the current (or the last) dump indexing */
@Value
@Builder
class DumpStatus {

    boolean running;

    int numPagesRead;

    int numPagesIndexed;

    int numPagesEstimated;

    @NonNull
    String dumpFileName;

    @NonNull
    LocalDateTime start;

    @Nullable
    LocalDateTime end;
}
