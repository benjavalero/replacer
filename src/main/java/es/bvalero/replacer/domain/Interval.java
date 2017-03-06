package es.bvalero.replacer.domain;

import java.util.List;

public class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isContained(List<Interval> intervals) {
        boolean isContained = false;
        for (Interval interval : intervals) {
            if (isContained(interval)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private boolean isContained(Interval interval2) {
        return this.getStart() >= interval2.getStart() && this.getEnd() <= interval2.getEnd();
    }

}
