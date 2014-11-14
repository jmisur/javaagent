import com.jmisur.Foo;
import com.jmisur.FooEx;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class MethodTest {
    @Rule
    public CaptureRule capture = new CaptureRule();

    @Test
    @CaptureRule.CompareTo("method-general.json")
    public void testFoo() throws IOException {
        Foo.main(new String[0]);
    }

    @Test(expected = RuntimeException.class)
    @CaptureRule.CompareTo("method-exception.json")
    public void testException() throws IOException {
        FooEx.main(new String[0]);
    }

}
