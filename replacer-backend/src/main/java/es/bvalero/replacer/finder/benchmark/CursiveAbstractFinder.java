package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class CursiveAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
