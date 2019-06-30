package es.bvalero.replacer.article;

public class ReplacementCount {

    private final String type;
    private final String subtype;
    private long count;

    public ReplacementCount(String type, String subtype, long count) {
        this.type = type;
        this.subtype = subtype;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public long getCount() {
        return count;
    }

    void setCount(long count) {
        this.count = count;
    }

}
