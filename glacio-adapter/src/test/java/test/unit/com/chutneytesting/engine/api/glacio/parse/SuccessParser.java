package test.unit.com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.api.glacio.parse.GlacioParser;
import com.github.fridujo.glacio.ast.Step;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SuccessParser extends GlacioParser {

    public SuccessParser() {
        super(null, null, null, null);
    }

    @Override
    public String parseTaskType(Step step) {
        return "success";
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(new Locale("tt", "TT"),
            new HashSet<>(Arrays.asList("SUCCESS", "DEBUG")));
        return keywords;
    }
}
