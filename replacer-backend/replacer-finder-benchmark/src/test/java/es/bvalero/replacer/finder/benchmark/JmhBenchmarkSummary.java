package es.bvalero.replacer.finder.benchmark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class JmhBenchmarkSummary {

    private String benchmark;
    private PrimaryMetric primaryMetric;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PrimaryMetric {

        private String score;
        private String scoreError;
    }
}
