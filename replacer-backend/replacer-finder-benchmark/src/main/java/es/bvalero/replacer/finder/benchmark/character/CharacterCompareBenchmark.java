package es.bvalero.replacer.finder.benchmark.character;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CharacterCompareBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "character/character-summary-jmh";

    private static final char ch1 = 'a';
    private static final char ch2 = 'b';
    private static final char ch3 = 'c';
    private static final char ch4 = 'd';
    private static final Set<Character> set1 = Set.of(ch1);
    private static final Set<Character> set2 = Set.of(ch1, ch2);
    private static final Set<Character> set3 = Set.of(ch1, ch2, ch3);
    private static final Set<Character> set4 = Set.of(ch1, ch2, ch3, ch4);
    private static final List<Character> list1 = List.of(ch1);
    private static final List<Character> list2 = List.of(ch1, ch2);
    private static final List<Character> list3 = List.of(ch1, ch2, ch3);
    private static final List<Character> list4 = List.of(ch1, ch2, ch3, ch4);
    private static final String word = "abecedario";

    @Override
    @Setup
    public void setUp() {
        // Base set-up
    }

    @Benchmark
    public void compareSet1(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(set1.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareSet2(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(set2.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareSet3(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(set3.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareSet4(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(set4.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareList1(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(list1.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareList2(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(list2.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareList3(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(list3.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareList4(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(list4.contains(word.charAt(i)));
        }
    }

    @Benchmark
    public void compareChars1(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(word.charAt(i) == ch1);
        }
    }

    @Benchmark
    public void compareChars2(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(word.charAt(i) == ch1 || word.charAt(i) == ch2);
        }
    }

    @Benchmark
    public void compareChars3(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(word.charAt(i) == ch1 || word.charAt(i) == ch2 || word.charAt(i) == ch3);
        }
    }

    @Benchmark
    public void compareChars4(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(
                word.charAt(i) == ch1 || word.charAt(i) == ch2 || word.charAt(i) == ch3 || word.charAt(i) == ch4
            );
        }
    }

    @Benchmark
    public void compareCharSequence1(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(compareChar(word.charAt(i), ch1));
        }
    }

    @Benchmark
    public void compareCharSequence2(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(compareChar(word.charAt(i), ch1, ch2));
        }
    }

    @Benchmark
    public void compareCharSequence3(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(compareChar(word.charAt(i), ch1, ch2, ch3));
        }
    }

    @Benchmark
    public void compareCharSequence4(Blackhole bh) {
        for (int i = 0; i < word.length(); i++) {
            bh.consume(compareChar(word.charAt(i), ch1, ch2, ch3, ch4));
        }
    }

    private boolean compareChar(char ch, char... chars) {
        for (char c : chars) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws RunnerException, ReplacerException {
        run(CharacterCompareBenchmark.class, fileName);

        generateChart(fileName);
    }
}
