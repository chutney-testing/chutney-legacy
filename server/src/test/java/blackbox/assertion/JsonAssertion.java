package blackbox.assertion;

import com.jayway.jsonpath.JsonPath;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class JsonAssertion extends AbstractAssert<JsonAssertion, String> {
    protected JsonAssertion(String actual) {
        super(actual, JsonAssertion.class);
    }

    public JsonAssertion hasPathEqualsTo(String path, String expected) {
        Object extract = JsonPath.read(actual, path);
        Assertions.assertThat(extract.toString()).as(path).isEqualTo(expected);
        return this;
    }

    public JsonAssertion hasPathSatisfy(String path, Consumer<Object> assertor) {
        Object extract = JsonPath.read(actual, path);
        assertor.accept(extract);
        return this;
    }
}
