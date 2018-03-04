package es.bvalero.replacer.dump;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Class to store the status and figures of the current dump indexation.
 */
public class DumpStatus {

    private static final double NUM_ARTICLES = 3557238; // Rough amount of articles to be checked

    private boolean running;
    private Date startDate;
    private Date endDate;

    private int numProcessedItems;

    public boolean isRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    public Date getStartDate() {
        return startDate;
    }

    void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getNumProcessedItems() {
        return numProcessedItems;
    }

    void setNumProcessedItems(int numProcessedItems) {
        this.numProcessedItems = numProcessedItems;
    }

    @SuppressWarnings("unused")
    public BigDecimal getPercentProgress() {
        double percentProgress = getNumProcessedItems() / NUM_ARTICLES * 100;
        // Format with two decimals
        return BigDecimal.valueOf(percentProgress).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @SuppressWarnings("unused")
    public Date getEstimatedFinishTime() {
        Date estimatedFinishTime = null;
        if (isRunning()) {
            double averageTimePerItem = (new Date().getTime() - getStartDate().getTime()) / (double) getNumProcessedItems();
            estimatedFinishTime = new Date(getStartDate().getTime() + (long) (averageTimePerItem * NUM_ARTICLES));
        }
        return estimatedFinishTime;
    }

}
