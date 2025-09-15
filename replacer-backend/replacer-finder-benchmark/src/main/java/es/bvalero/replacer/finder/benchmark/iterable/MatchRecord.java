package es.bvalero.replacer.finder.benchmark.iterable;

record MatchRecord(int start, String text) {
    int end() {
        return start + text.length();
    }
}
