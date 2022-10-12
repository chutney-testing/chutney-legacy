package com.chutneytesting.action.assertion.compare;

public class CompareActionFactory {

    private CompareActionFactory() {}

    public static CompareExecutor createCompareAction(String mode) {
        if ("equals".equalsIgnoreCase(mode)) {
            return new CompareEqualsAction();
        }
        if (isEquals(mode, "not-equals", "not equals")) {
            return new CompareNotEqualsAction();
        }
        if ("contains".equalsIgnoreCase(mode)) {
            return new CompareContainsAction();
        }
        if (isEquals(mode, "not-contains", "not contains")) {
            return new CompareNotContainsAction();
        }
        if (isEquals(mode, "greater-than", "greater than")) {
            return new CompareGreaterThanAction();
        }
        if (isEquals(mode, "less-than", "less than")) {
            return new CompareLessThanAction();
        }
        return new NoCompareAction();
    }

    private static boolean isEquals(String mode, String s, String s2) {
        return s.equalsIgnoreCase(mode) || s2.equalsIgnoreCase(mode);
    }
}
