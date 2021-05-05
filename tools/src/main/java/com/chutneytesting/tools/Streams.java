package com.chutneytesting.tools;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Streams {

    private Streams() {
    }

    public static <T> Function<T, T> identity(Consumer<T> action) {
        return t -> {
            action.accept(t);
            return t;
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, Object, Optional<T>> collectUniqueResult() {
        return Collectors.collectingAndThen(
            (Collector) Collectors.<T>toList(),
            (List<T> list) -> {
                if (list.size() > 1) {
                    throw new IllegalArgumentException("Found " + list.size() + " result");
                } else if (list.size() == 1) {
                    return Optional.of(list.get(0));
                }
                return Optional.empty();
            }
        );
    }

    public static <T> Stream<T> toStream(final Enumeration<T> enumeration) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                new Iterator<>() {
                    public T next() {
                        return enumeration.nextElement();
                    }

                    public boolean hasNext() {
                        return enumeration.hasMoreElements();
                    }
                },
                Spliterator.ORDERED), false);
    }

    public static <T> Optional<T> findLast(Stream<T> source, Predicate<T> condition) {
        return source
            .filter(condition)
            .reduce((first, second) -> second);
    }

    public static <T> Stream<T> takeWhile(Stream<T> source, Predicate<T> condition) {
        return StreamSupport.stream(TakeWhileSpliterator.over(source.spliterator(), condition), false)
            .onClose(source::close);
    }

    public static <T> Stream<T> takeUntil(Stream<T> source, Predicate<T> condition) {
        return takeWhile(source, condition.negate());
    }

    public static <T> Stream<T> skipUntil(Stream<T> source, Predicate<T> condition) {
        return StreamSupport.stream(SkipUntilSpliterator.over(source.spliterator(), condition), false)
            .onClose(source::close);
    }
}
