package com.chutneytesting.design.domain.editionlock;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Predicate;

public interface TestCaseEditions {

    List<TestCaseEdition> findAll();

    boolean add(TestCaseEdition testCaseEdition);

    boolean remove(TestCaseEdition testCaseEdition);

    default List<TestCaseEdition> findBy(Predicate<TestCaseEdition> condition) {
        return findAll().stream()
            .filter(condition)
            .collect(toList());
    }
}
