import com.jmisur.Foo;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class TransformerTest {
    @Rule
    public CaptureRule capture = new CaptureRule();

    @Test
    @CaptureRule.CompareTo("transformerTest.json")
    public void testFoo() throws IOException {
        Foo.main(new String[0]);
    }

}
