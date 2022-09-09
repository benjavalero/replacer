package es.bvalero.replacer;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderService;
import es.bvalero.replacer.finder.replacement.RemoveObsoleteReplacementType;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "es.bvalero.replacer")
class ArchitectureTest {

    private static final String[] COMMON_PACKAGES = { "java..", "lombok..", "org.apache.commons..", "org.slf4j.." };

    private static String[] commonPackagesAnd(String... packages) {
        return ArrayUtils.addAll(packages, COMMON_PACKAGES);
    }

    // Package dependency checks

    @ArchTest
    static final ArchRule domainAccess = classes()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .onlyAccessClassesThat()
        .resideInAnyPackage(commonPackagesAnd("..common.."));

    @ArchTest
    static final ArchRule authenticationAccess = classes()
        .that()
        .resideInAPackage("..authentication..")
        .should()
        .onlyBeAccessed()
        .byClassesThat()
        .resideInAnyPackage("..authentication..");

    @ArchTest
    static final ArchRule adminAccess = classes()
        .that()
        .resideInAPackage("..admin..")
        .should()
        .onlyBeAccessed()
        .byClassesThat()
        .resideInAnyPackage("..admin..");

    @ArchTest
    static final ArchRule finderAccess = classes()
        .that()
        .resideInAPackage("..finder..")
        .and()
        .doNotImplement(FinderService.class)
        .and()
        .doNotHaveSimpleName(RemoveObsoleteReplacementType.class.getSimpleName())
        .should()
        .onlyHaveDependentClassesThat(
            JavaClass.Predicates
                .resideInAnyPackage("..finder..")
                .or(JavaClass.Predicates.simpleName(ArchitectureTest.class.getSimpleName()))
        );

    // Naming classes

    @ArchTest
    static final ArchRule finderSuffix = classes()
        .that()
        .implement(Finder.class)
        .should()
        .haveSimpleNameEndingWith("Finder");

    @ArchTest
    static final ArchRule finderServiceSuffix = classes()
        .that()
        .implement(FinderService.class)
        .should()
        .haveSimpleNameEndingWith("FinderService");

    @ArchTest
    static final ArchRule restControllerSuffix = classes()
        .that()
        .areAnnotatedWith(RestController.class)
        .should()
        .haveSimpleNameEndingWith("Controller");

    // Naming methods

    @ArchTest
    static final ArchRule noRetrieveMethods = noMethods().should().haveNameStartingWith("retrieve");

    @ArchTest
    static final ArchRule optionalReturn = methods()
        .that()
        .haveNameStartingWith("get")
        .should()
        .notHaveRawReturnType(Optional.class);

    // Tests

    @ArchTest
    static final ArchRule testMethods = methods()
        .that()
        .areAnnotatedWith(Test.class)
        .should()
        .haveNameStartingWith("test")
        .andShould()
        .notHaveModifier(JavaModifier.PUBLIC)
        .andShould()
        .haveRawReturnType("void");

    @ArchTest
    static final ArchRule testClasses = classes()
        .that()
        .haveSimpleNameEndingWith("Test")
        .and()
        .haveSimpleNameNotEndingWith("jmhTest")
        .should()
        .notHaveModifier(JavaModifier.PUBLIC);

    // Configuration

    @ArchTest
    static final ArchRule configurationClasses = classes()
        .that()
        .areAnnotatedWith(Configuration.class)
        .should()
        .haveSimpleNameEndingWith("Configuration")
        .andShould()
        .resideInAPackage("es.bvalero.replacer.config")
        .andShould()
        .haveModifier(JavaModifier.PUBLIC);
}
