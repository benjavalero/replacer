package es.bvalero.replacer.finder.benchmark.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class DomainFinder {
    final static Set<String> SUFFIXES = new HashSet<>(Arrays.asList("com", "es", "gov", "info", "org"));

    abstract Set<String> findMatches(String text);
}
