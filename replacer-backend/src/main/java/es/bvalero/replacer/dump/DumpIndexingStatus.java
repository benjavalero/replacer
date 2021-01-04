package es.bvalero.replacer.dump;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DumpIndexingStatus {

    private boolean running;
    private long numPagesRead;
    private long numPagesProcessed;
    private long numPagesEstimated;
    private String dumpFileName;
    private long start;
    private Long end;

    DumpIndexingStatus(String dumpFileName, long numPagesEstimated) {
        this.numPagesEstimated = numPagesEstimated;
        this.dumpFileName = dumpFileName;
        this.start = Instant.now().toEpochMilli();

        // Default values
        this.running = true;
        this.numPagesRead = 0L;
        this.numPagesProcessed = 0L;
        this.end = null;
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
