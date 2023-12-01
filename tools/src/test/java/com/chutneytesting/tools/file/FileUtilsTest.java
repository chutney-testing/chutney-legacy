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

package com.chutneytesting.tools.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

    @RepeatedTest(5)
    void init_folder_should_support_parallel_execution(@TempDir Path tmpDir) {
        int nbSync = 2;
        Path aDirectory = tmpDir.resolve("aDirectory");
        CyclicBarrier sync = new CyclicBarrier(nbSync);
        List<Exception> exceptions = new ArrayList<>();
        Runnable run = () -> {
            try {
                sync.await();
                FileUtils.initFolder(aDirectory);
            } catch (Exception e) {
                exceptions.add(e);
            }
        };

        Collection<Thread> threads = IntStream.range(0, nbSync).mapToObj(i -> new Thread(run)).toList();
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                exceptions.add(e);
            }
        });

        assertThat(exceptions).isEmpty();
    }
}
