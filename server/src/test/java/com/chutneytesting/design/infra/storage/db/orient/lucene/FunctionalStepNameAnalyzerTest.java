package com.chutneytesting.design.infra.storage.db.orient.lucene;

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

public class FunctionalStepNameAnalyzerTest {

    @Test
    public void should_not_do_crazy_things() throws IOException {
        FunctionalStepNameAnalyzer sut = new FunctionalStepNameAnalyzer();
        checkRandomData(new Random(), sut, 5000);
    }

    @Test
    public void should_analyze_with_consistency() throws IOException {
        FunctionalStepNameAnalyzer sut = new FunctionalStepNameAnalyzer();
        checkAnalysisConsistency(new Random(), sut, false, "This is a big functional step to tokenize ...");
    }

    @Test
    public void should_not_tokenize_gwt_stop_words() throws IOException {
        FunctionalStepNameAnalyzer sut = new FunctionalStepNameAnalyzer();

        CharArraySet frenchGwtStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(FunctionalStepNameAnalyzer.class,
            "french_gwt.txt", StandardCharsets.UTF_8));
        for (Object o : frenchGwtStopWords) {
            assertTokenStreamContents(
                sut.tokenStream("name", new String((char[])o) + " une belle étape avec une belle description importante"),
                new String[]{"belle", "étape", "belle", "description", "importante"}
            );
        }

        CharArraySet englishGwtStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(FunctionalStepNameAnalyzer.class,
            "english_gwt.txt", StandardCharsets.UTF_8));
        for (Object o : englishGwtStopWords) {
            assertTokenStreamContents(
                sut.tokenStream("name", new String((char[])o) + " a nice step with a nice important description"),
                new String[]{"nice", "step", "nice", "important", "description"}
            );
        }
    }

}
