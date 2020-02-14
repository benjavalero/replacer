package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class FileAbstractFinder {

    abstract Set<FinderResult> findMatches(String text);
}
