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
