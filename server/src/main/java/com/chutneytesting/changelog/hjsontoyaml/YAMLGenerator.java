package com.chutneytesting.changelog.hjsontoyaml;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import java.io.IOException;
import java.io.Writer;
import org.yaml.snakeyaml.DumperOptions;

class YAMLGenerator extends com.fasterxml.jackson.dataformat.yaml.YAMLGenerator {
    private final static DumperOptions.ScalarStyle STYLE_QUOTED = DumperOptions.ScalarStyle.DOUBLE_QUOTED;
    private final static DumperOptions.ScalarStyle STYLE_PLAIN = DumperOptions.ScalarStyle.PLAIN;
    private final static DumperOptions.ScalarStyle STYLE_LITERAL = DumperOptions.ScalarStyle.LITERAL;

    public YAMLGenerator(IOContext ctxt, int jsonFeatures, int yamlFeatures, StringQuotingChecker quotingChecker, ObjectCodec codec, Writer out, DumperOptions.Version version) throws IOException {
        super(ctxt, jsonFeatures, yamlFeatures, quotingChecker, codec, out, version);
    }

    /**
     * When {@link com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature#MINIMIZE_QUOTES} is enabled, use LITERAL style when quoting is necessary
     */
    @Override
    public void writeString(String text) throws IOException, JsonGenerationException {
        if (text == null) {
            writeNull();
            return;
        }
        _verifyValueWrite("write String value");

        // [dataformats-text#50]: Empty String always quoted
        if (text.isEmpty()) {
            _writeScalar(text, "string", STYLE_QUOTED);
            return;
        }

        DumperOptions.ScalarStyle style;
        if (Feature.MINIMIZE_QUOTES.enabledIn(_formatFeatures)) {
            if (text.indexOf('\n') >= 0 || _quotingChecker.needToQuoteValue(text)) {
                style = STYLE_LITERAL;
                // If one of reserved values ("true", "null"), or, number, preserve quoting:
            } else if (Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS.enabledIn(_formatFeatures)
                && PLAIN_NUMBER_P.matcher(text).matches()
            ) {
                style = STYLE_QUOTED;
            } else {
                style = STYLE_PLAIN;
            }
        } else {
            if (Feature.LITERAL_BLOCK_STYLE.enabledIn(_formatFeatures)
                && text.indexOf('\n') >= 0) {
                style = STYLE_LITERAL;
            } else {
                style = STYLE_QUOTED;
            }
        }
        _writeScalar(text, "string", style);
    }
}
