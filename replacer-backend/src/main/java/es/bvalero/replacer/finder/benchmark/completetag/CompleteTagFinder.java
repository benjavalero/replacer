package es.bvalero.replacer.finder.benchmark.completetag;

import java.util.Set;

abstract class CompleteTagFinder {

    abstract Set<String> findMatches(String text);
}
