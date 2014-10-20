import com.jmisur.Foo;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import java.io.IOException;
import java.io.StringWriter;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

public class TransformerTest {

    @Test
    public void testFoo() throws IOException, JSONException {
        StringWriter writer = new StringWriter();
        SimpleMain.init(writer);
        SimpleMain.start();
        Foo.main(new String[0]);
        SimpleMain.stop();
        JSONCompareResult compareResult = JSONCompare.compareJSON(
                new JSONArray(IOUtils.toString(getClass().getResourceAsStream("capture.json"))),
                new JSONArray(writer.toString()),
                new DefaultComparator(LENIENT) {
                    @Override
                    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
                        if (prefix.endsWith("millis") || prefix.endsWith("systemId")) return;
                        super.compareValues(prefix, expectedValue, actualValue, result);
                    }
                });

        if (compareResult.failed()) throw new AssertionError(compareResult.getMessage());
    }

}
