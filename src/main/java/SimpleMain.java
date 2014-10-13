import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class SimpleMain {
    private static ObjectMapper mapper = new ObjectMapper();
    private static File file = new File("capture.json");

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        instrumentation.addTransformer(new SimpleTransformer());
    }

    public static void capture(String methodName, String[] paramNames, Object[] paramValues) {
        MethodInvocation mi = new MethodInvocation();
        mi.setParams(paramNames, paramValues);
        mi.setSignature(methodName);
        mi.setThreadName(Thread.currentThread().getName());

        log(mi);
    }

    private static void log(Object o) {
        try {
            mapper.writer().withDefaultPrettyPrinter().writeValue(file, o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}