package es.bvalero.replacer.finder.benchmark.completetag;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class CompleteTagIndexOfLinearFinder extends CompleteTagFinder {
    private final Set<String> words;

    CompleteTagIndexOfLinearFinder(List<String> words) {
        this.words = new HashSet<>(words);
    }

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        int start = 0;
        while (start >= 0) {
            start = text.indexOf('<', start);
            if (start >= 0) {
                int endOpenTag = text.indexOf('>', start);
                if (endOpenTag >= 0) {
                    String openTag = text.substring(start + 1, endOpenTag);
                    Optional<String> tag = words.stream().filter(openTag::startsWith).findAny();
                    if (tag.isPresent()) {
                        String closeTag = String.format("</%s>", tag.get());
                        int startCloseTag = text.indexOf(closeTag, endOpenTag);
                        if (startCloseTag >= 0) {
                            int endCloseTag = startCloseTag + closeTag.length();
                            String completeTag = text.substring(start, endCloseTag);
                            matches.add(completeTag);
                            start = endCloseTag + 1;
                        } else {
                            start++;
                        }
                    } else {
                        start++;
                    }
                } else {
                    start++;
                }
            }
        }
        return matches;
    }
}
