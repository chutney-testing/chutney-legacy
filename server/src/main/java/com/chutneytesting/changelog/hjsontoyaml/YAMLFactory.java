package com.chutneytesting.changelog.hjsontoyaml;

import com.fasterxml.jackson.core.io.IOContext;
import java.io.IOException;
import java.io.Writer;

public class YAMLFactory extends com.fasterxml.jackson.dataformat.yaml.YAMLFactory {
    @Override
    protected YAMLGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        int feats = _yamlGeneratorFeatures;
        YAMLGenerator gen = new YAMLGenerator(ctxt, _generatorFeatures, feats,
            _quotingChecker, _objectCodec, out, _version);
        // any other initializations? No?
        return gen;
    }
}
