package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class FileAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
