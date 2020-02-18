package com.chutneytesting.engine.domain.execution;


import com.chutneytesting.engine.domain.execution.event.Event;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {

    private static final RxBus INSTANCE = new RxBus();

    public static RxBus getInstance() {
        return INSTANCE;
    }

    private Subject<Object> bus = PublishSubject.create().toSerialized();

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
