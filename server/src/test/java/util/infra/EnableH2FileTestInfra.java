package util.infra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DisplayName("H2 (file)")
@ActiveProfiles({"test-infra-h2", "test-infra-h2-file"})
public @interface EnableH2FileTestInfra {
}
