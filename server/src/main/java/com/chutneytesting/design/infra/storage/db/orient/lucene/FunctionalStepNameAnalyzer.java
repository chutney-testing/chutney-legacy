package com.chutneytesting.design.infra.storage.db.orient.lucene;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.apache.lucene.util.IOUtils;

/**
 * Lucene analyzer combining French and English analyzers whitout stemming filtering.
 *
 * @see org.apache.lucene.analysis.fr.FrenchAnalyzer
 * @see org.apache.lucene.analysis.en.EnglishAnalyzer
 */
public class FunctionalStepNameAnalyzer extends StopwordAnalyzerBase {

    private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

    /**
     * Returns an unmodifiable instance of the default stop-words set.
     * @return an unmodifiable instance of the default stop-words set.
     */
    public static CharArraySet getDefaultStopSet() {
        return FunctionalStepNameAnalyzer.DefaultSetHolder.DEFAULT_STOP_SET;
    }

    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;
        static {
            try {
                CharArraySet frenchStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(SnowballFilter.class,
                    FrenchAnalyzer.DEFAULT_STOPWORD_FILE, StandardCharsets.UTF_8));

                CharArraySet englishStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(SnowballFilter.class,
                        "english_stop.txt", StandardCharsets.UTF_8));

                CharArraySet frenchGwtStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(FunctionalStepNameAnalyzer.class,
                    "french_gwt.txt", StandardCharsets.UTF_8));

                CharArraySet englishGwtStopWords = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(FunctionalStepNameAnalyzer.class,
                    "english_gwt.txt", StandardCharsets.UTF_8));

                DEFAULT_STOP_SET = new CharArraySet(frenchStopWords, false);
                DEFAULT_STOP_SET.addAll(englishStopWords);
                DEFAULT_STOP_SET.addAll(frenchGwtStopWords);
                DEFAULT_STOP_SET.addAll(englishGwtStopWords);

            } catch (IOException ex) {
                // default set should always be present as it is part of the
                // distribution (JAR)
                throw new RuntimeException("Unable to load default stopword set");
            }
        }
    }

    /**
     * Builds an analyzer with the default stop words ({@link #getDefaultStopSet}).
     */
    public FunctionalStepNameAnalyzer() {
        this(FunctionalStepNameAnalyzer.DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words
     *
     * @param stopwords
     *          a stopword set
     */
    public FunctionalStepNameAnalyzer(CharArraySet stopwords) {
        super(stopwords);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final StandardTokenizer src = new StandardTokenizer(); // todo - no need to end and close ?
        src.setMaxTokenLength(maxTokenLength);
        TokenStream result = new StandardFilter(src); // todo - no need to end and close ?
        result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES);
        result = new EnglishPossessiveFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopwords);
        return new TokenStreamComponents(src, result) {
            @Override
            protected void setReader(final Reader reader) {
                // So that if maxTokenLength was changed, the change takes
                // effect next time tokenStream is called:
                src.setMaxTokenLength(FunctionalStepNameAnalyzer.this.maxTokenLength);
                super.setReader(reader);
            }
        };
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new StandardFilter(in); // todo - no need to end and close ?
        result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES);
        result = new LowerCaseFilter(result);
        return result;
    }
}
