package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.FileUtils;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.replacement.Misspelling;
import es.bvalero.replacer.finder.replacement.MisspellingManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImmutableFinderService implements FinderService<Immutable> {

    @Autowired
    private List<ImmutableFinder> immutableFinders;

    @PostConstruct
    public void sortImmutableFinders() {
        Collections.sort(immutableFinders);
    }

    @Override
    public List<Finder<Immutable>> getFinders() {
        return new ArrayList<>(immutableFinders);
    }

    @TestOnly
    public static Set<String> getFalsePositives() throws ReplacerException {
        String text = FileUtils.getFileContent("/offline/false-positives.txt");
        FalsePositiveManager falsePositiveManager = new FalsePositiveManager();
        return falsePositiveManager.parseItemsText(text);
    }

    @TestOnly
    public static Set<String> getUppercaseMisspellings() throws ReplacerException {
        String text = FileUtils.getFileContent("/offline/misspelling-list.txt");
        MisspellingManager misspellingManager = new MisspellingManager();
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        UppercaseAfterFinder uppercaseAfterFinder = new UppercaseAfterFinder();
        return uppercaseAfterFinder.getUppercaseWords(misspellings);
    }
}
