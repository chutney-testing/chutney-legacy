package com.chutneytesting.tools.loader;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Load objects from abstract sources.<br>
 * Mechanism similar to {@link java.util.ServiceLoader} or {@link org.springframework.core.io.support.SpringFactoriesLoader} but more extensible.
 *
 * @param <EXTENSION> type of object to load
 */
public interface ExtensionLoader<EXTENSION> {

    /**
     * @return a {@link Set} of loaded Objects
     */
    Set<EXTENSION> load();

    /**
     * Source from where to fetch Object description.
     *
     * @param <SOURCE> intermediate type to map to <b>EXTENSION</b> objects
     */
    interface ExtensionLoaderSource<SOURCE> {
        /**
         * @return a {@link Set} of description that will be later mapped to <b>EXTENSION</b> objects
         */
        Set<SOURCE> load();
    }

    /**
     * Builder of {@link ExtensionLoader}.
     *
     * @see #withSource(ExtensionLoaderSource)
     */
    class Builder<SOURCE, EXTENSION> {

        private final ExtensionLoaderSource<SOURCE> source;
        private Function<SOURCE, Set<EXTENSION>> mappingFunction;

        private Builder(ExtensionLoaderSource<SOURCE> source) {
            this.source = source;
        }

        /**
         * @return an {@link Builder} using the given {@link ExtensionLoaderSource}
         */
        public static <SOURCE, EXTENSION> Builder<SOURCE, EXTENSION> withSource(ExtensionLoaderSource<SOURCE> source) {
            return new Builder<>(source);
        }

        /**
         * @return an {@link ExtensionLoader} using the given <b>mappingFunction</b> to transform descriptions loaded by {@link ExtensionLoaderSource}
         */
        public ExtensionLoader<EXTENSION> withMapper(Function<SOURCE, Set<EXTENSION>> mappingFunction) {
            this.mappingFunction = mappingFunction;
            return build();
        }

        private ExtensionLoader<EXTENSION> build() {
            return () -> source.load().stream().flatMap(mappingFunction.andThen(Set::stream)).collect(Collectors.toSet());
        }
    }
}
