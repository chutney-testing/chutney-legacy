package com.chutneytesting.tools;

import com.google.common.cache.CacheLoader;
import java.util.function.Function;

/**
 * Bridge between {@link Function} and {@link CacheLoader}.<br>
 * {@link CacheLoader} has the same erasure as {@link Function}, but as an <i>abstract class</i> it cannot be created without initialization (constructor).
 */
class FunctionCacheLoader<K, V> extends CacheLoader<K, V> {

    private final Function<K, V> loader;

    FunctionCacheLoader(Function<K, V> loader) {
        this.loader = loader;
    }


    @Override
    public V load(K key) throws Exception {
        return loader.apply(key);
    }
}
