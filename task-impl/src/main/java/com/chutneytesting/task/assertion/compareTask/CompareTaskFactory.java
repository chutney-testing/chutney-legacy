package com.chutneytesting.task.assertion.compareTask;

public class CompareTaskFactory {

    private CompareTaskFactory() {}

    public static CompareExecutor createCompareTask(String mode) {
        if ("equals".equalsIgnoreCase(mode)) {
            return new CompareEqualsTask();
        }
        if (isEquals(mode, "not-equals", "not equals")) {
            return new CompareNotEqualsTask();
        }
        if ("contains".equalsIgnoreCase(mode)) {
            return new CompareContainsTask();
        }
        if (isEquals(mode, "not-contains", "not contains")) {
            return new CompareNotContainsTask();
        }
        if (isEquals(mode, "greater-than", "greater than")) {
            return new CompareGreaterThanTask();
        }
        if (isEquals(mode, "less-than", "less than")) {
            return new CompareLessThanTask();
        }
        return new NoCompareTask();
    }

    private static boolean isEquals(String mode, String s, String s2) {
        return s.equalsIgnoreCase(mode) || s2.equalsIgnoreCase(mode);
    }
}
