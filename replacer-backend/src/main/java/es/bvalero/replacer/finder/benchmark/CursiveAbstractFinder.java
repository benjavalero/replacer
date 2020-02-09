package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class CursiveAbstractFinder {

    abstract Set<FinderResult> findMatches(String text);

}
