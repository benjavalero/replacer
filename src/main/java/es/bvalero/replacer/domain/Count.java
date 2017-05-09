package es.bvalero.replacer.domain;

import java.io.Serializable;

public class Count implements Serializable {

    private String value;
    private Long count;

    public Count(String value, Long count) {
        this.value = value;
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
