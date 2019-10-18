package es.bvalero.replacer.dump;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
class DumpIndexation {
    private boolean running;
    private boolean forceProcess;
    private long numArticlesRead;
    private long numArticlesProcessable;
    private long numArticlesProcessed;
    private String dumpFileName;
    private long start;
    private Long end;

    DumpIndexation(String dumpFileName, boolean forceProcess) {
        this.forceProcess = forceProcess;
        this.dumpFileName = dumpFileName;
        this.start = Instant.now().toEpochMilli();

        // Default values
        this.running = true;
        this.numArticlesRead = 0L;
        this.numArticlesProcessable = 0L;
        this.numArticlesProcessed = 0L;
        this.end = null;
    }

    void finish() {
        this.running = false;
        this.end = Instant.now().toEpochMilli();
    }

    void incrementNumArticlesRead() {
        this.numArticlesRead++;
    }

    void incrementNumArticlesProcessable() {
        this.numArticlesProcessable++;
    }

    void incrementNumArticlesProcessed() {
        this.numArticlesProcessed++;
    }

}
