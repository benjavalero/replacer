package es.bvalero.replacer;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "es.bvalero.replacer")
public class ArchitectureTest {

    private static final String[] COMMON_PACKAGES = {"java..", "lombok..", "org.apache.commons.."};

    private static String[] commonPackagesAnd(String... packages) {
        return ArrayUtils.addAll(packages, COMMON_PACKAGES);
    }

    // Package dependency checks

    @ArchTest
    public static final ArchRule domainAccessesOnlyCommon = classes()
        .that().resideInAPackage("..domain..")
        .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..common.."));

    // Naming classes

    @ArchTest
    public static final ArchRule finderSuffix = classes()
        .that().implement(Finder.class)
        .should().haveSimpleNameEndingWith("Finder");

    @ArchTest
    public static final ArchRule finderServiceSuffix = classes()
        .that().implement(FinderService.class)
        .should().haveSimpleNameEndingWith("FinderService");

    @ArchTest
    public static final ArchRule restControllerSuffix = classes()
        .that().areAnnotatedWith(RestController.class)
        .should().haveSimpleNameEndingWith("Controller");

    // Naming methods

    @ArchTest
    public static final ArchRule noRetrieveMethods = noMethods()
        .should().haveNameStartingWith("retrieve");

    @ArchTest
    public static final ArchRule testMethods = methods()
        .that().areAnnotatedWith(Test.class)
        .should().haveNameStartingWith("test");
}
