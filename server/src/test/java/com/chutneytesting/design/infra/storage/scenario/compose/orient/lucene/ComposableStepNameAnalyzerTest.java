package com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene;

import static org.apache.lucene.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;
import static org.apache.lucene.analysis.BaseTokenStreamTestCase.checkAnalysisConsistency;
import static org.apache.lucene.analysis.BaseTokenStreamTestCase.checkRandomData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.junit.Test;

public class ComposableStepNameAnalyzerTest {

    @Test
    public void should_not_do_crazy_things() throws IOException {
        ComposableStepNameAnalyzer sut = new ComposableStepNameAnalyzer();
        checkRandomData(new Random(), sut, 5000);
    }

    @Test
    public void should_analyze_with_consistency() throws IOException {
        ComposableStepNameAnalyzer sut = new ComposableStepNameAnalyzer();
        checkAnalysisConsistency(new Random(), sut, false, "This is a big functional step to tokenize ...");
    }

    @Test
    public void should_not_tokenize_gwt_stop_words() throws IOException {
        ComposableStepNameAnalyzer sut = new ComposableStepNameAnalyzer();

        CharArraySet frenchGwtStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(ComposableStepNameAnalyzer.class,
            "french_gwt.txt", StandardCharsets.UTF_8));
        for (Object o : frenchGwtStopWords) {
            assertTokenStreamContents(
                sut.tokenStream("name", new String((char[])o) + " une belle étape avec une belle description importante"),
                new String[]{"belle", "étape", "belle", "description", "importante"}
            );
        }

        CharArraySet englishGwtStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(ComposableStepNameAnalyzer.class,
            "english_gwt.txt", StandardCharsets.UTF_8));
        for (Object o : englishGwtStopWords) {
            assertTokenStreamContents(
                sut.tokenStream("name", new String((char[])o) + " a nice step with a nice important description"),
                new String[]{"nice", "step", "nice", "important", "description"}
            );
        }
    }

}
