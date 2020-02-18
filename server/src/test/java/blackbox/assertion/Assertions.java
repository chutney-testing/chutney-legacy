package blackbox.assertion;

public class Assertions {
    public static JsonAssertion assertThatJson(String actual) {
        return new JsonAssertion(actual);
    }
}
