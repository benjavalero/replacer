package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class CommentAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

}
