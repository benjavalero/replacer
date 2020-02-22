package es.bvalero.replacer.finder.benchmark.completetag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CompleteTagIndexOfFinder extends CompleteTagFinder {
    private final List<String> words;

    CompleteTagIndexOfFinder(List<String> words) {
        this.words = words;
    }

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        for (String word : words) {
            String openTag = String.format("<%s", word);
            String closeTag = String.format("</%s>", word);
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(openTag, start);
                if (start >= 0) {
                    int endOpenTag = text.indexOf('>', start + openTag.length());
                    if (endOpenTag >= 0) {
                        String openTagContent = text.substring(start, endOpenTag);
                        if (openTagContent.contains("/")) {
                            start += openTag.length();
                        } else {
                            int startCloseTag = text.indexOf(closeTag, start + openTag.length());
                            if (startCloseTag >= 0) {
                                int endCloseTag = startCloseTag + closeTag.length();
                                matches.add(text.substring(start, endCloseTag));
                                start = endCloseTag + 1;
                            } else {
                                start += openTag.length();
                            }
                        }
                    } else {
                        start += openTag.length();
                    }
                }
            }
        }
        return matches;
    }
}
