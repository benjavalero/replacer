package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.SequencedCollection;

class TemplateStructureFinder {

    static SequencedCollection<TemplateStructure> findNext(FinderPage page, int index) {
        final List<TemplateStructure> matches = new ArrayList<>();

        final String text = page.getContent();
        final Deque<TemplateStructure> matchStack = new ArrayDeque<>();
        for (int i = index; i < text.length() - 1; i++) {
            final char ch = text.charAt(i);
            if (ch == '{' && text.charAt(i + 1) == '{') {
                matchStack.addLast(new TemplateStructure(text, i));
                i++; // skip second '{'
            } else if (!matchStack.isEmpty()) {
                final TemplateStructure current = matchStack.getLast();
                if (ch == '}' && text.charAt(i + 1) == '}') {
                    closeName(text, current, i);
                    matchStack.removeLast();
                    closeStructure(text, current, i);
                    matches.add(current);
                    if (matchStack.isEmpty()) {
                        // We found a complete structure, so we can return it
                        break;
                    }
                    matchStack.getLast().addNested(current);
                    i++; // skip second '}'
                } else if (current.startName() < 0 && !Character.isWhitespace(ch)) {
                    // First non-whitespace char after '{{': start of name
                    current.setNameStart(i);
                } else if (current.endName() < 0) {
                    // Still scanning the name
                    if (ch == PIPE || ch == COLON) {
                        closeName(text, current, i);
                    }
                }
            } else {
                // No structure open yet: skip to next '{{'
                final int nextStart = text.indexOf(START_TEMPLATE, i);
                if (nextStart < 0) {
                    break;
                }
                i = nextStart - 1; // -1 because the for loop will increment
            }
        }

        if (!matchStack.isEmpty()) {
            final TemplateStructure unclosed = matchStack.getFirst();
            if (isNotFakeTemplate(text, unclosed.start())) {
                FinderUtils.logFinderResult(
                    page,
                    unclosed.start(),
                    unclosed.start() + START_TEMPLATE.length(),
                    "Structure not closed"
                );
            }
        }

        return matches;
    }

    private static void closeName(String text, TemplateStructure structure, int end) {
        if (structure.startName() >= 0 && structure.endName() < 0) {
            structure.setNameEnd(FinderUtils.trimRight(text, structure.startName(), end));
        }
    }

    private static void closeStructure(String text, TemplateStructure structure, int end) {
        structure.setEnd(end + END_TEMPLATE.length());
    }

    private static boolean isNotFakeTemplate(String text, int startTemplate) {
        // There are some cases where curly braces inside a LaTeX formula may be confused with template start
        // We want to avoid the warning in these cases
        // This method is only called in case the template is not closed
        final char nextChar = text.charAt(startTemplate + START_TEMPLATE.length());
        return FinderUtils.isWordChar(nextChar);
    }
}
