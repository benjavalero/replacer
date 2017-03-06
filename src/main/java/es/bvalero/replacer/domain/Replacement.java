package es.bvalero.replacer.domain;

public class Replacement implements Comparable<Replacement> {

    private Integer position;
    private String word;
    private String fix;
    private String explain;
    private boolean fixed;

    public Replacement() {
    }

    public Replacement(Integer position, String word) {
        this.position = position;
        this.word = word;
        this.fixed = false;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getFix() {
        return fix;
    }

    public void setFix(String fix) {
        this.fix = fix;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    @Override
    public int compareTo(Replacement r2) {
        return r2.getPosition() - this.getPosition();
    }

}
