package com.chutneytesting.task.amqp.consumer;


import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.chutneytesting.task.TestLogger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConsumerSupervisorTest {

    public static final int LOCK_WAITING = 500;
    ConsumerSupervisor consumerSupervisor = ConsumerSupervisor.getInstance();

    @Test
    public void test_lock_unlock() {
        final String queue = "name";
        final String anotherQueue = "other name";

        assertThat(consumerSupervisor.isLocked(queue)).isFalse();
        assertThat(consumerSupervisor.isLocked(anotherQueue)).isFalse();

        assertThat(consumerSupervisor.lock(queue)).isTrue();

        assertThat(consumerSupervisor.isLocked(queue)).isTrue();
        assertThat(consumerSupervisor.isLocked(anotherQueue)).isFalse();

        consumerSupervisor.unlock(queue);

        assertThat(consumerSupervisor.isLocked(queue)).isFalse();
        assertThat(consumerSupervisor.isLocked(anotherQueue)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(longs = {-100, 0, 200, 1000, 10000})
    public void should_lock_and_not_wait(long duration) throws InterruptedException {
        final String queue = "name";
        consumerSupervisor.unlock(queue);

        Pair<Boolean, Long> result = consumerSupervisor.waitUntilQueueAvailable(queue, duration, new TestLogger());
        consumerSupervisor.unlock(queue);

        boolean locked = result.getLeft();
        assertThat(locked).isTrue();
        long timeLeft = result.getRight();
        assertThat(timeLeft).isEqualTo(duration);
    }

    @ParameterizedTest
    @ValueSource(longs = {-100, 0, 200, 1000})
    public void should_wait(long duration) throws InterruptedException {
        final String queue = "name";
        consumerSupervisor.lock(queue);

        Pair<Boolean, Long> result = consumerSupervisor.waitUntilQueueAvailable(queue, duration, new TestLogger());
        consumerSupervisor.unlock(queue);

        boolean locked = result.getLeft();
        assertThat(locked).isFalse();
        long timeLeft = result.getRight();
        assertThat(timeLeft).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(longs = {-100, 0, 300, 600, 745, 1000})
    public void should_set_timeleft_depending_of_duration_for_one_lock(long duration) throws InterruptedException {
        final String queue = "name";
        consumerSupervisor.lock(queue);

        AtomicReference<Pair<Boolean, Long>> result = new AtomicReference<>();
        new Thread(() -> {
            try {
                result.set(consumerSupervisor.waitUntilQueueAvailable(queue, duration, new TestLogger()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        TimeUnit.MILLISECONDS.sleep(400);
        consumerSupervisor.unlock(queue);

        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(result.get()).isNotNull();
            }
        );
        boolean locked = result.get().getLeft();
        if (duration < LOCK_WAITING) {
            assertThat(locked).isFalse();
        } else {
            assertThat(locked).isTrue();
        }
        long timeLeft = result.get().getRight();
        long expected = duration > LOCK_WAITING ? duration - LOCK_WAITING : 0;
        assertThat(timeLeft).isEqualTo(expected);
    }


}
