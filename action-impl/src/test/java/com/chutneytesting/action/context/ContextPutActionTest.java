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

package com.chutneytesting.action.context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.injectable.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ContextPutActionTest {

    @Test
    public void should_not_throw_NullPointerException() {
        Map<String, Object> entries = new HashMap<>();
        entries.put("null value", null);
        entries.put("string", "some text");
        entries.put("list", Lists.newArrayList(1,2,3,4));
        entries.put("object", new FailAction(null));
        Map<Object, Object> map = Maps.newHashMap();
        map.put("some key", "some value");
        entries.put("map", map);
        entries.put("primitive", 3);
        entries.put("wrapper", Integer.MAX_VALUE);
        Logger logger = mock(Logger.class);
        Action contextPut = new ContextPutAction(logger, entries);

        contextPut.execute();

        verify(logger, times(7)).info(any());
    }
}
