/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Dominic Fox
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.chutneytesting.tools;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

class SkipUntilSpliterator<T> implements Spliterator<T>  {

    static <T> SkipUntilSpliterator<T> over(Spliterator<T> source, Predicate<T> condition) {
        return new SkipUntilSpliterator<>(source, condition, false);
    }

    static <T> SkipUntilSpliterator<T> overInclusive(Spliterator<T> source, Predicate<T> condition) {
        return new SkipUntilSpliterator<>(source, condition, true);
    }

    private final Spliterator<T> source;
    private final Predicate<T> condition;
    private final boolean inclusive;
    private boolean conditionMet = false;

    private SkipUntilSpliterator(Spliterator<T> source, Predicate<T> condition, boolean inclusive) {
        this.source = source;
        this.condition = condition;
        this.inclusive = inclusive;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (conditionMet) {
            return source.tryAdvance(action);
        }
        while (!conditionMet && source.tryAdvance(e -> {
            if (condition.test(e)) {
                if (inclusive) {
                    action.accept(e);
                }
                conditionMet = true;
            }
        }));
        return conditionMet;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if (!conditionMet) {
            tryAdvance(action);
        }
        if (conditionMet) {
            source.forEachRemaining(action);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return conditionMet ? source.estimateSize() : Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return source.characteristics() &~ Spliterator.SIZED;
    }

    @Override
    public Comparator<? super T> getComparator() {
        return source.getComparator();
    }
}
