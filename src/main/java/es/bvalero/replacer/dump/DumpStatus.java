package es.bvalero.replacer.dump;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Class to store the status and figures of the current dump indexation.
 */
public class DumpStatus {

    // Rough amount of pages to be checked
    // Type double to make easier calculations with decimals
    private static final double NUM_TOTAL_PAGES = 3666708;

    private boolean running;
    private Long startDate;
    private Long endDate;
    private int pagesCount;
    private boolean processOldArticles;

    void start() {
        this.running = true;
        this.startDate = System.currentTimeMillis();
        this.endDate = null;
        this.pagesCount = 0;
    }

    void increase() {
        this.pagesCount++;
    }

    void finish() {
        this.running = false;
        this.endDate = System.currentTimeMillis();
    }

    /* PUBLIC ACCESSORS */

    public boolean isRunning() {
        return this.running;
    }

    public double getProgress() {
        double percentProgress = pagesCount / NUM_TOTAL_PAGES * 100;
        // Format with two decimals
        return Math.round(percentProgress * 100) / 100.0;
    }

    public int getPagesCount() {
        return this.pagesCount;
    }

    @Nullable
    public Long getEta() {
        Long average = getAverage();
        if (average == null) {
            return null;
        } else {
            long numToProcess = Math.round(NUM_TOTAL_PAGES - pagesCount);
            return numToProcess * average;
        }
    }

    @Nullable
    public Long getAverage() {
        if (pagesCount == 0) {
            return null;
        } else if (isRunning()) {
            return (System.currentTimeMillis() - startDate) / pagesCount;
        } else if (endDate == null) {
            return null;
        } else {
            return (endDate - startDate) / pagesCount;
        }
    }

    @Nullable
    public Long getLastRun() {
        return this.endDate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isRunning()) {
            sb.append("Indexation is running\n")
                    .append("Progress: ").append(getProgress()).append("\n")
                    .append("Pages processed: ").append(pagesCount).append("\n")
                    .append("ETA: ").append(getEta()).append("\n")
                    .append("Average Time: ").append(getAverage()).append("\n");
        } else {
            sb.append("Indexation is not running\n")
                    .append("Pages processed: ").append(pagesCount).append("\n")
                    .append("Average Time: ").append(getAverage()).append("\n")
                    .append("Last Run: ").append(new Date(endDate));
        }
        return sb.toString();
    }

    public boolean isProcessOldArticles() {
        return processOldArticles;
    }

    void setProcessOldArticles(boolean processOldArticles) {
        this.processOldArticles = processOldArticles;
    }

}
