package com.chutneytesting.tools;

import com.google.common.cache.CacheLoader;
import java.util.function.Function;

/**
 * Utilities around Guava's.
 */
public class Guavas {

    private Guavas() {}

    /**
     * @return a {@link CacheLoader} using the given {@link Function} to provide values
     */
    public static <K, V> CacheLoader<K, V> cacheLoader(Function<K, V> loader) {
        return new FunctionCacheLoader<>(loader);
    }
}
