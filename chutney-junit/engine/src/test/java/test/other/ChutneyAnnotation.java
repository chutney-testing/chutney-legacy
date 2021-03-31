package test.other;

import com.chutneytesting.junit.api.AfterAll;
import com.chutneytesting.junit.api.BeforeAll;
import com.chutneytesting.junit.api.Chutney;

@Chutney
public class ChutneyAnnotation {

    @BeforeAll
    public void setUp() {
        // Nothing to do
    }

    @AfterAll
    public void tearDown() {
        // Nothing to do
    }

}
