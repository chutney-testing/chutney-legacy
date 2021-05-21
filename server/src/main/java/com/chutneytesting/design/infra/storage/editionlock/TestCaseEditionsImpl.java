package com.chutneytesting.design.infra.storage.editionlock;

import static com.chutneytesting.ServerConfiguration.EDITIONS_TTL_UNIT_SPRING_VALUE;
import static com.chutneytesting.ServerConfiguration.EDITIONS_TTL_VALUE_SPRING_VALUE;

import com.chutneytesting.design.domain.editionlock.TestCaseEdition;
import com.chutneytesting.design.domain.editionlock.TestCaseEditions;
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
        @Value(EDITIONS_TTL_VALUE_SPRING_VALUE) Integer ttlValue,
        @Value(EDITIONS_TTL_UNIT_SPRING_VALUE) String ttlUnit
    ) {
        editions = CacheBuilder.newBuilder()
            .expireAfterWrite(ttlValue, TimeUnit.valueOf(ttlUnit))
            .build(new CacheLoader<>() {
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
