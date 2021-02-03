package es.bvalero.replacer.dump;

import java.time.Instant;
import lombok.Getter;
import org.jetbrains.annotations.TestOnly;

@Getter
final class DumpIndexingStatus {

    private boolean running;
    private long numPagesRead;
    private long numPagesProcessed;
    private final long numPagesEstimated;
    private final String dumpFileName;
    private final long start;
    private Long end;

    private DumpIndexingStatus(boolean running, String dumpFileName, long numPagesEstimated) {
        this.running = running;
        this.numPagesEstimated = numPagesEstimated;
        this.dumpFileName = dumpFileName;
        this.start = Instant.now().toEpochMilli();

        // Default values
        this.numPagesRead = 0L;
        this.numPagesProcessed = 0L;
        this.end = null;
    }

    @TestOnly
    DumpIndexingStatus(
        boolean running,
        long numPagesRead,
        long numPagesProcessed,
        long numPagesEstimated,
        String dumpFileName,
        long start,
        Long end
    ) {
        this.running = running;
        this.numPagesRead = numPagesRead;
        this.numPagesProcessed = numPagesProcessed;
        this.numPagesEstimated = numPagesEstimated;
        this.dumpFileName = dumpFileName;
        this.start = start;
        this.end = end;
    }

    static DumpIndexingStatus of(String dumpFileName, long numPagesEstimated) {
        return new DumpIndexingStatus(true, dumpFileName, numPagesEstimated);
    }

    static DumpIndexingStatus ofEmpty() {
        return new DumpIndexingStatus(false, "", 0L);
    }

    void finish() {
        this.running = false;
        this.end = Instant.now().toEpochMilli();
    }

    void incrementNumPagesRead() {
        this.numPagesRead++;
    }

    void incrementNumPagesProcessed() {
        this.numPagesProcessed++;
    }
}
