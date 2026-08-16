package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.SequencedCollection;

/** Snapshot of {@link TemplateStructureFinder} as it was at the time the benchmark was introduced. */
class TemplateStructureFinderOld {

    static SequencedCollection<TemplateStructureOld> findNext(FinderPage page, int index) {
        // A loop is a little better than recursion
        final List<TemplateStructureOld> matches = new ArrayList<>();

        final String text = page.getContent();
        // Deque implementation is a little better than old stack and recommended by Java
        // We store the start of the structure but we also store the groups found inside
        final Deque<TemplateStructureOld> matchStack = new ArrayDeque<>();
        while (index >= 0 && index < text.length()) {
            if (matchStack.isEmpty()) {
                final int newStart = findStructureStart(text, index);
                if (newStart < 0) {
                    break;
                }
                matchStack.addLast(openStructure(text, newStart));
                index = newStart + START_TEMPLATE.length();
            }

            assert !matchStack.isEmpty();
            final TemplateStructureOld currentMatch = matchStack.getLast();
            final int start = currentMatch.start();
            final int end = findStructureEnd(text, index);
            if (end < 0) {
                // Structure not closed. Not worth keep on searching as the next structures are considered as nested.
                if (isNotFakeTemplate(text, start)) {
                    FinderUtils.logFinderResult(page, start, start + START_TEMPLATE.length(), "Structure not closed");
                }
                break;
            }

            final int nextStart = findStructureStart(text, index);
            if (nextStart >= 0 && nextStart < end) {
                // Nested structure
                matchStack.addLast(openStructure(text, nextStart));
                index = nextStart + START_TEMPLATE.length();
            } else {
                closeStructure(text, currentMatch, end);
                if (!validateSpecialCharacters(text, currentMatch)) {
                    matchStack.removeLast();
                    if (matchStack.isEmpty()) {
                        index = currentMatch.end();
                        continue;
                    }
                } else {
                    matches.add(currentMatch);
                    matchStack.removeLast();
                    if (!matchStack.isEmpty()) {
                        // Add the ended Match to the parent that now it's at the top of the stack
                        matchStack.getLast().addNested(currentMatch);
                    } else {
                        // We found a complete structure, so we can return it
                        break;
                    }
                }
                index = currentMatch.end();
            }
        }
        return matches;
    }

    private static int findStructureStart(String text, int index) {
        return text.indexOf(START_TEMPLATE, index);
    }

    private static int findStructureEnd(String text, int index) {
        return text.indexOf(END_TEMPLATE, index);
    }

    private static TemplateStructureOld openStructure(String text, int start) {
        return new TemplateStructureOld(text, start);
    }

    private static void closeStructure(String text, TemplateStructureOld structure, int end) {
        structure.setEnd(end + END_TEMPLATE.length());
        final String name = findTemplateName(text, structure);
        structure.setName(name);
        structure.setNormalizedName(ReplacerUtils.toLowerCase(name).replace('_', ' '));
    }

    private static String findTemplateName(String text, TemplateStructureOld structure) {
        final int startTemplateContent = structure.start() + START_TEMPLATE.length();
        final int endTemplateContent = structure.end() - END_TEMPLATE.length();
        final int startName = FinderUtils.trimLeft(text, startTemplateContent, endTemplateContent);
        final int startPipe = text.indexOf(PIPE, startName, endTemplateContent);
        final int endTitle = startPipe >= 0 ? startPipe : endTemplateContent;
        final int startColon = text.indexOf(COLON, startName, endTitle);
        final int endName = FinderUtils.trimRight(text, startName, startColon >= 0 ? startColon : endTitle);
        return text.substring(startName, endName);
    }

    private static boolean validateSpecialCharacters(String text, TemplateStructureOld structure) {
        // Check that not all the characters are an escaped-pipe. See: https://en.wikipedia.org/wiki/Template:!!
        final int start = structure.start() + START_TEMPLATE.length();
        final int end = structure.end() - END_TEMPLATE.length();
        for (int i = start; i < end; i++) {
            if (text.charAt(i) != '!') {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotFakeTemplate(String text, int templateStart) {
        // There are some cases where curly braces inside a LaTeX formula may be confused with template start
        // We want to avoid the warning in these cases
        // This method is only called in case the template is not closed
        final char nextChar = text.charAt(templateStart + START_TEMPLATE.length());
        return FinderUtils.isWordChar(nextChar);
    }
}
