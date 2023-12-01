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

package com.chutneytesting.action.amqp.consumer;


import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.chutneytesting.action.TestLogger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConsumerSupervisorTest {

    private static final int LOCK_WAITING = 500;
    private final ConsumerSupervisor consumerSupervisor = ConsumerSupervisor.getInstance();

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
    @ValueSource(longs = {-100, 0, 300})
    public void should_set_timeleft_to_zero_for_duration_under_supervisor_lock_waiting(long duration) throws InterruptedException {
        assertThat(duration < LOCK_WAITING).isTrue();

        String queue = "name_" + duration;

        consumerSupervisor.lock(queue);
        Pair<Boolean, Long> result = consumerSupervisor.waitUntilQueueAvailable(queue, duration, new TestLogger());
        consumerSupervisor.unlock(queue);

        boolean locked = result.getLeft();
        assertThat(locked).isFalse();

        long timeLeft = result.getRight();
        assertThat(timeLeft).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = {600, 745, 1000})
    public void should_set_timeleft_for_duration_above_supervisor_lock_waiting(long duration) throws InterruptedException {
        assertThat(duration > LOCK_WAITING).isTrue();

        String queue = "name_" + duration;
        AtomicReference<Pair<Boolean, Long>> result = new AtomicReference<>();
        Thread waitThread = new Thread(() -> {
            try {
                result.set(consumerSupervisor.waitUntilQueueAvailable(queue, duration, new TestLogger()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        consumerSupervisor.lock(queue);
        waitThread.start();
        TimeUnit.MILLISECONDS.sleep(400);
        consumerSupervisor.unlock(queue);

        await().atMost(5, SECONDS).untilAtomic(result, notNullValue(Pair.class));

        boolean locked = result.get().getLeft();
        assertThat(locked).isTrue();

        long timeLeft = result.get().getRight();
        assertThat((duration - timeLeft) % LOCK_WAITING).isZero();
    }
}
