package com.chutneytesting.design.domain.scenario.compose;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public enum StepUsage {

    GIVEN(Arrays.asList("etant donne", "given")),
    WHEN(Arrays.asList("quand", "when")),
    THEN(Arrays.asList("alors", "then")),
    STEP(Collections.emptyList());

    private static final List<String> andWords = Arrays.asList("et", "and");
    private List<String> startUsageWords;

    StepUsage(List<String> startUsageWords) {
        this.startUsageWords = startUsageWords;
    }

    public boolean isStartUsageWords(String normalizedSentence) {
        return startUsageWords.stream().anyMatch(normalizedSentence::startsWith);
    }

    public static Optional<StepUsage> fromName(String name) {
        try {
            return Optional.of(StepUsage.valueOf(name));
        } catch (IllegalArgumentException e) {
            try {
                return Optional.of(StepUsage.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ee) {
                return Optional.empty();
            }
        }
    }

    public static Optional<StepUsage> fromSentence(String sentence, Optional<StepUsage> previousSiblingUsage) {
        String normalizedSentence = StringUtils.stripAccents(sentence.trim()).toLowerCase();

        Optional<StepUsage> stepUsage = Arrays.stream(StepUsage.values()).filter(
            s -> s.isStartUsageWords(normalizedSentence)
        ).findFirst();

        if (stepUsage.isPresent()) {
            return stepUsage;
        }

        if (previousSiblingUsage.isPresent()) {
            Optional<String> startWithAnd = andWords.stream().filter(normalizedSentence::startsWith).findFirst();
            if(startWithAnd.isPresent()){
                return previousSiblingUsage;
            }
        }

        return Optional.empty();
    }

    public String removeStartOrAndWord(String sentence) {
        String normalizedSentence = StringUtils.stripAccents(sentence.trim());

        for (String startWord : startUsageWords) {
            if (normalizedSentence.startsWith(startWord)) {
                return sentence.substring(startWord.length());
            }
        }

        for (String andWord : andWords) {
            if (normalizedSentence.startsWith(andWord)) {
                return sentence.substring(andWord.length());
            }
        }

        return sentence;
    }
}
