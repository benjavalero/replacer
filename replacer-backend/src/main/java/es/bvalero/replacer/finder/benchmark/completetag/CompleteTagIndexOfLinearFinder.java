package es.bvalero.replacer.finder.benchmark.completetag;

import java.util.HashSet;
import java.util.List;
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
                int startOpenTag = start++;
                // Find the tag
                StringBuilder tagBuilder = new StringBuilder();
                char ch = text.charAt(start);
                while (Character.isLetter(ch)) {
                    tagBuilder.append(ch);
                    ch = text.charAt(++start);
                }
                String tag = tagBuilder.toString();
                if (words.contains(tag)) {
                    // Find end of open tag
                    boolean slashFound = false;
                    while (start < text.length()) {
                        char ch2 = text.charAt(start);
                        if (ch2 == '/') {
                            slashFound = true;
                        } else if (ch2 == '>') {
                            break;
                        }
                        start++;
                    }
                    if (start == text.length()) {
                        start = -1;
                    } else if (!slashFound) {
                        String closeTag = new StringBuilder("</").append(tag).append('>').toString();
                        int startCloseTag = text.indexOf(closeTag, start);
                        if (startCloseTag >= 0) {
                            int endCloseTag = startCloseTag + closeTag.length();
                            String completeTag = text.substring(startOpenTag, endCloseTag);
                            matches.add(completeTag);
                            start = endCloseTag + 1;
                        }
                    }
                }
            }
        }
        return matches;
    }
}
