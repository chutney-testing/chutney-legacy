/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.domain.execution;


import com.chutneytesting.engine.domain.execution.event.Event;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class RxBus {

    private static final RxBus INSTANCE = new RxBus();

    public static RxBus getInstance() {
        return INSTANCE;
    }

    private final Subject<Object> bus = PublishSubject.create().toSerialized();

    public void post(Object event) {
        bus.onNext(event);
    }

    public <T> Disposable register(final Class<T> eventClass, Consumer<T> onNext) {
        return bus
            .filter(event -> event.getClass().equals(eventClass))
            .map(obj -> (T) obj)
            .subscribe(onNext);
    }

    public <T extends Event> Disposable registerOnExecutionId(final Class<T> eventClass, long executionId, Consumer<? super Event> onNext) {
        return bus
            .filter(event -> event.getClass().equals(eventClass))
            .map(obj -> (T) obj)
            .filter(e -> e.executionId() == executionId)
            .subscribe(onNext);
    }

    public Observable<Object> toObservable() {
        return bus;
    }
}
