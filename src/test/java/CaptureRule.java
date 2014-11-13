import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.regex.Pattern;

public class CaptureRule implements MethodRule {
    private StringWriter writer;
    private boolean debug;


    public CaptureRule() {
    }

    public CaptureRule(boolean debug) {
        this.debug = debug;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(method);
                try {
                    base.evaluate();
                } finally {
                    after(method);
                }
            }
        };
    }

    private void before(FrameworkMethod method) throws IOException {
        if (debug)
            SimpleMain.start(System.out, false);
        else {
            writer = new StringWriter();
            SimpleMain.start(writer);
        }
    }

    private void after(FrameworkMethod method) throws IOException {
        SimpleMain.stop();
        if (debug) return;

        String jsonFile = originalFileName(method);
        if (jsonFile != null) {
            String original = original(jsonFile);
            List<String> originalList = sanitize(toLines(original));
            String revised = writer.toString();
            List<String> revisedList = sanitize(toLines(revised));

            Patch<String> patch = DiffUtils.diff(originalList, revisedList);
            List<String> strings = DiffUtils.generateUnifiedDiff(null, null, originalList, patch, 2);
            check(jsonFile, strings);
        }
    }

    private void check(String jsonFile, List<String> strings) {
        if (strings == null || strings.size() == 0) return;
        String result = FluentIterable.from(strings).skip(2).join(Joiner.on("\n"));
        Assert.fail("Diff [" + jsonFile + "]:\n" + result);
    }

    private List<String> sanitize(List<String> strings) {
        final Pattern millis = Pattern.compile("\\s*\"millis\"\\s*:.*");
        final Pattern correlationId = Pattern.compile("\\s*\"correlationId\"\\s*:.*");
        final Pattern systemId = Pattern.compile("\\s*\"systemId\"\\s*:.*");
        final Pattern sqlId = Pattern.compile("\\s*\"sqlId\"\\s*:.*");

        // TODO assertions on these fields (long value / string length etc)
        return FluentIterable.from(strings).filter(new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return !(millis.matcher(s).matches()
                        || correlationId.matcher(s).matches()
                        || systemId.matcher(s).matches()
                        || sqlId.matcher(s).matches());
            }
        }).toList();
    }

    private List<String> toLines(String json) throws IOException {
        return IOUtils.readLines(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    private String original(String jsonFile) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(jsonFile)) {
            return IOUtils.toString(is);
        } catch (NullPointerException e) {
            throw new RuntimeException("JSON file not found: " + jsonFile);
        }
    }

    private String originalFileName(FrameworkMethod method) {
        CompareTo annotation = method.getAnnotation(CompareTo.class);
        return annotation == null ? null : annotation.value();
    }

    public void disable() {
        SimpleMain.pause();
    }

    public void enable() {
        SimpleMain.resume();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface CompareTo {
        String value();
    }
}
