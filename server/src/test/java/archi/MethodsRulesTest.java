package archi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = {
    "com.chutneytesting",
})
public class MethodsRulesTest {

    @ArchTest
    static final ArchRule controller_methods_are_secured = methods()
        .that()
        .arePublic()
        .should()
        .onlyBeCalled()
        //classe diff || (classe diff && meme classe)
        .byClassesThat(DescribedPredicate.describe("", claz -> ))

        // used
        .shou()
        .beprivate
}
