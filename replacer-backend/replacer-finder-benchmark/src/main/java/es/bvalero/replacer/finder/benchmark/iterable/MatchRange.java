package es.bvalero.replacer.finder.benchmark.iterable;

record MatchRange(int start, int end) {
    String toSubstring(String text) {
        return text.substring(start, end);
    }
}
