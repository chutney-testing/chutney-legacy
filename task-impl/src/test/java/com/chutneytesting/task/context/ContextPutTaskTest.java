package com.chutneytesting.task.context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.injectable.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ContextPutTaskTest {

    @Test
    public void should_not_throw_NullPointerException() {
        Map<String, Object> entries = new HashMap<>();
        entries.put("null value", null);
        entries.put("string", "some text");
        entries.put("list", Lists.newArrayList(1,2,3,4));
        entries.put("object", new FailTask(null));
        Map<Object, Object> map = Maps.newHashMap();
        map.put("some key", "some value");
        entries.put("map", map);
        entries.put("primitive", 3);
        entries.put("wrapper", Integer.MAX_VALUE);
        Logger logger = mock(Logger.class);
        Task contextPut = new ContextPutTask(logger, entries);

        contextPut.execute();

        verify(logger, times(7)).info(any());
    }
}
