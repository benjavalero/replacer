package es.bvalero.replacer.dump;

public final class DumpProcessStatus {
    private final boolean running;
    private final boolean forceProcess;
    private final long numArticlesRead;
    private final long numArticlesProcessed;
    private final String dumpFileName;
    private final Long start;
    private final Long end;

    private DumpProcessStatus(boolean running, boolean forceProcess, long numArticlesRead, long numArticlesProcessed,
                              String dumpFileName, Long start, Long end) {
        this.running = running;
        this.forceProcess = forceProcess;
        this.numArticlesRead = numArticlesRead;
        this.numArticlesProcessed = numArticlesProcessed;
        this.dumpFileName = dumpFileName;
        this.start = start;
        this.end = end;
    }

    static DumpProcessStatus.DumpProcessStatusBuilder builder() {
        return new DumpProcessStatus.DumpProcessStatusBuilder();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isForceProcess() {
        return forceProcess;
    }

    public long getNumArticlesRead() {
        return numArticlesRead;
    }

    public long getNumArticlesProcessed() {
        return numArticlesProcessed;
    }

    public String getDumpFileName() {
        return dumpFileName;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    static class DumpProcessStatusBuilder {
        private boolean running;
        private boolean forceProcess;
        private long numArticlesRead;
        private long numArticlesProcessed;
        private String dumpFileName;
        private Long start;
        private Long end;

        DumpProcessStatus.DumpProcessStatusBuilder setRunning(boolean running) {
            this.running = running;
            return this;
        }

        DumpProcessStatus.DumpProcessStatusBuilder setForceProcess(boolean forceProcess) {
            this.forceProcess = forceProcess;
            return this;
        }

        DumpProcessStatus.DumpProcessStatusBuilder setNumArticlesRead(long numArticlesRead) {
            this.numArticlesRead = numArticlesRead;
            return this;
        }

        DumpProcessStatus.DumpProcessStatusBuilder setNumArticlesProcessed(long numArticlesProcessed) {
            this.numArticlesProcessed = numArticlesProcessed;
            return this;
        }

        DumpProcessStatus.DumpProcessStatusBuilder setDumpFileName(String dumpFileName) {
            this.dumpFileName = dumpFileName;
            return this;
        }

        DumpProcessStatus.DumpProcessStatusBuilder setStart(Long start) {
            this.start = start;
            return this;
        }

        DumpProcessStatus.DumpProcessStatusBuilder setEnd(Long end) {
            this.end = end;
            return this;
        }

        DumpProcessStatus build() {
            return new DumpProcessStatus(running, forceProcess, numArticlesRead, numArticlesProcessed, dumpFileName, start, end);
        }

    }

}
