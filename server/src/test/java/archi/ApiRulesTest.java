package archi;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

@AnalyzeClasses(packages = {
    "com.chutneytesting",
})
public class ApiRulesTest {

    @ArchTest
    static final ArchRule controller_methods_are_secured = methods()
        .that()
        .areDeclaredInClassesThat()
        .areAnnotatedWith(RestController.class)
        .and()
        .areAnnotatedWith(
            DescribedPredicate.describe("Annotation ends with Mapping.", javaAnnotation -> javaAnnotation.getType().getName().endsWith("Mapping")))
        .should()
        .beAnnotatedWith(PreAuthorize.class);

    @ArchTest
    static final ArchRule controller_methods_returns_dto = freeze(methods()
        .that()
        .areDeclaredInClassesThat()
        .areAnnotatedWith(RestController.class)
        .and()
        .areAnnotatedWith(
            DescribedPredicate.describe("Annotation ends with Mapping.", javaAnnotation -> javaAnnotation.getType().getName().endsWith("Mapping")))
        .should()
        .notHaveRawReturnType(
            DescribedPredicate.describe(
                "in domain",
                javaClass -> javaClass.getPackageName().contains(".domain.")
            )
        ));
}
