package com.chutneytesting.design.infra.testcase;

import com.chutneytesting.design.domain.testcase.TestCaseEdition;
import com.chutneytesting.design.domain.testcase.TestCaseEditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class TestCaseEditionsImpl implements TestCaseEditions {

    private final LoadingCache<TestCaseEdition, TestCaseEdition> editions;

    public TestCaseEditionsImpl(
        @Value("${iceberg.editions.ttl.value:6}") Integer ttlValue,
        @Value("${iceberg.editions.ttl.unit:HOURS}") String ttlUnit
    ) {
        editions = CacheBuilder.newBuilder()
            .expireAfterWrite(ttlValue, TimeUnit.valueOf(ttlUnit))
            .build(new CacheLoader<TestCaseEdition, TestCaseEdition>() {
                @Override
                public TestCaseEdition load(TestCaseEdition key) throws Exception {
                    return key;
                }
            });
    }

    @Override
    public List<TestCaseEdition> findAll() {
        return new ArrayList<>(editions.asMap().values());
    }

    @Override
    public boolean add(TestCaseEdition testCaseEdition) {
        editions.getUnchecked(testCaseEdition);
        return true;
    }

    @Override
    public boolean remove(TestCaseEdition testCaseEdition) {
        editions.invalidate(testCaseEdition);
        return true;
    }
}
