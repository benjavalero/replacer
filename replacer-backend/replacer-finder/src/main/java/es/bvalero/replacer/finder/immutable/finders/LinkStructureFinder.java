package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.SequencedCollection;

class LinkStructureFinder {

    static SequencedCollection<LinkStructure> findNext(FinderPage page, int index) {
        // A loop is a little better than recursion
        final List<LinkStructure> matches = new ArrayList<>();

        final String text = page.getContent();
        // Deque implementation is a little better than old stack and recommended by Java
        // We store the start of the structure but we also store the groups found inside
        final Deque<LinkStructure> matchStack = new ArrayDeque<>();
        for (int i = index; i < text.length() - 1; i++) {
            final char ch = text.charAt(i);
            if (ch == '[' && text.charAt(i + 1) == '[') {
                final LinkStructure structure = new LinkStructure(text, i);
                matchStack.addLast(structure);
                i++; // skip second '['
            } else if (!matchStack.isEmpty()) {
                final LinkStructure current = matchStack.getLast();
                if (ch == ']' && text.charAt(i + 1) == ']') {
                    current.setEnd(i + END_LINK.length());
                    matches.add(current);
                    matchStack.removeLast();
                    if (matchStack.isEmpty()) {
                        // We found a complete structure, so we can return it
                        break;
                    }
                    i++; // skip second ']'
                } else if (ch == PIPE) {
                    current.setPipePos(i);
                } else if (ch == COLON && current.posColon() < 0 && current.posPipe() < 0) {
                    current.setColonPos(i);
                }
            } else if (matchStack.isEmpty()) {
                // No structure open yet: skip to next '[['
                final int nextStart = text.indexOf(START_LINK, i);
                if (nextStart < 0) {
                    break;
                }
                i = nextStart - 1; // -1 because the for loop will increment
            }
        }

        if (!matchStack.isEmpty()) {
            final LinkStructure unclosed = matchStack.getFirst();
            FinderUtils.logFinderResult(
                page,
                unclosed.start(),
                unclosed.start() + START_LINK.length(),
                "Structure not closed"
            );
        }

        return matches;
    }
}
