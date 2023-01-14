package es.bvalero.replacer.dump;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value
@Builder
class DumpIndexingStatus {

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

    static DumpIndexingStatus ofEmpty() {
        return DumpIndexingStatus.builder().running(false).build();
    }
}
