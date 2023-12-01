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

package test.com.chutneytesting.junit.engine;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.chutneytesting.junit.engine.ChutneyEngineDescriptor;
import com.chutneytesting.junit.engine.ChutneyEngineExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import test.other.ChutneyAnnotation;

public class ChutneyEngineDescriptorTest {

    private ChutneyEngineExecutionContext context;

    @BeforeEach
    public void setUp() {
        context = mock(ChutneyEngineExecutionContext.class);
    }

    @Test
    public void should_invoke_beforeAll_hook_when_class_annoted_and_children_exists() throws Exception {
        ChutneyAnnotation chutneyClass = mock(ChutneyAnnotation.class);
        ChutneyEngineDescriptor sut = new ChutneyEngineDescriptor(UniqueId.forEngine("engine"), "name", chutneyClass);
        sut.addChild(mock(TestDescriptor.class));

        sut.before(context);

        verify(chutneyClass).setUp();
    }

    @Test
    public void should_invoke_afterAll_hook_class_annotated_and_children_exists() throws Exception {
        ChutneyAnnotation chutneyClass = mock(ChutneyAnnotation.class);
        ChutneyEngineDescriptor sut = new ChutneyEngineDescriptor(UniqueId.forEngine("engine"), "name", chutneyClass);
        sut.addChild(mock(TestDescriptor.class));

        sut.after(context);

        verify(chutneyClass).tearDown();
    }
}
