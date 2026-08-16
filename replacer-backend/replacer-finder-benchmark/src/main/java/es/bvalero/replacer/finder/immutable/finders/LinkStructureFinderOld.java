package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.SequencedCollection;

/** Snapshot of {@link LinkStructureFinder} as it was at the time the benchmark was introduced. */
class LinkStructureFinderOld {

    static SequencedCollection<LinkStructure> findNext(FinderPage page, int index) {
        // A loop is a little better than recursion
        final List<LinkStructure> matches = new ArrayList<>();

        final String text = page.getContent();
        // Deque implementation is a little better than old stack and recommended by Java
        // We store the start of the structure but we also store the groups found inside
        final Deque<LinkStructure> matchStack = new ArrayDeque<>();
        while (index >= 0 && index < text.length()) {
            if (matchStack.isEmpty()) {
                final int newStart = findStructureStart(text, index);
                if (newStart < 0) {
                    break;
                }
                matchStack.addLast(openStructure(text, newStart));
                index = newStart + START_LINK.length();
            }

            assert !matchStack.isEmpty();
            final LinkStructure currentMatch = matchStack.getLast();
            final int start = currentMatch.start();
            final int end = findStructureEnd(text, index);
            if (end < 0) {
                // Structure not closed. Not worth keep on searching as the next structures are considered as nested.
                FinderUtils.logFinderResult(page, start, start + START_LINK.length(), "Structure not closed");
                break;
            }

            final int nextStart = findStructureStart(text, index);
            if (nextStart >= 0 && nextStart < end) {
                // Nested structure
                matchStack.addLast(openStructure(text, nextStart));
                index = nextStart + START_LINK.length();
            } else {
                closeStructure(text, currentMatch, end);
                matches.add(currentMatch);
                matchStack.removeLast();
                if (!matchStack.isEmpty()) {
                    // Add the ended Match to the parent that now it's at the top of the stack
                    matchStack.getLast().addNested(currentMatch);
                } else {
                    // We found a complete structure, so we can return it
                    break;
                }
                index = currentMatch.end();
            }
        }
        return matches;
    }

    private static int findStructureStart(String text, int index) {
        return text.indexOf(START_LINK, index);
    }

    private static int findStructureEnd(String text, int index) {
        return text.indexOf(END_LINK, index);
    }

    private static LinkStructure openStructure(String text, int start) {
        final LinkStructure structure = new LinkStructure(text, start);
        // Find ':' immediately after '[[' — it always precedes any nested structure or '|'
        // We store it unconditionally and validate against the actual end when closing
        final int colonPos = text.indexOf(COLON, start + START_LINK.length());
        structure.setColonPos(colonPos);
        return structure;
    }

    private static void closeStructure(String text, LinkStructure structure, int end) {
        findPipePos(text, structure, end);
        validateColonPos(structure, end);
        structure.setEnd(end + END_LINK.length());
    }

    private static void findPipePos(String text, LinkStructure structure, int end) {
        // Find the last '|' in the current structure content
        final int pipePos = text.lastIndexOf(PIPE, end - 1);
        if (pipePos >= structure.start() + START_LINK.length()) {
            structure.setPipePos(pipePos);
        }
    }

    private static void validateColonPos(LinkStructure structure, int end) {
        // Reset colonPos if it falls outside the link title
        final int titleEnd = structure.pipePos() >= 0 ? structure.pipePos() : end;
        if (structure.colonPos() >= titleEnd) {
            structure.setColonPos(-1);
        }
    }
}
