package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class CommentAbstractFinder {

    abstract Set<FinderResult> findMatches(String text);

}
