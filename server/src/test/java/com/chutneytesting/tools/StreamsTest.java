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

package com.chutneytesting.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class StreamsTest {

    @Test
    public void build_a_finite_stream_based_on_enumeration() {
        Vector<String> items = new Vector<>(Arrays.asList("test1", "test2"));

        Stream<String> stringStream = Streams.toStream(items.elements());

        assertThat(stringStream.count()).isEqualTo(2);
    }
}
