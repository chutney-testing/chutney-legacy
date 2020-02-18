package com.chutneytesting.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Stream;
import com.chutneytesting.tools.Streams;
import org.junit.Test;

public class StreamsTest {

    @Test
    public void build_a_finite_stream_based_on_enumeration() {
        Vector<String> items = new Vector<>(Arrays.asList("test1", "test2"));

        Stream<String> stringStream = Streams.toStream(items.elements());

        assertThat(stringStream.count()).isEqualTo(2);
    }
}
