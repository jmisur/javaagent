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
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.regex.Pattern;

public class CaptureRule implements MethodRule {
    private StringWriter writer;

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after(method);
                }
            }
        };
    }

    private void before() throws IOException {
        writer = new StringWriter();
        SimpleMain.start(writer);
    }

    private void after(FrameworkMethod method) throws IOException {
        SimpleMain.stop();
        String original = original(method);
        List<String> originalList = sanitize(toLines(original));
        String revised = writer.toString();
        List<String> revisedList = sanitize(toLines(revised));

        Patch patch = DiffUtils.diff(originalList, revisedList);
        List<String> strings = DiffUtils.generateUnifiedDiff(null, null, originalList, patch, 2);
        check(strings);
    }

    private void check(List<String> strings) {
        if (strings == null || strings.size() == 0) return;
        String result = FluentIterable.from(strings).skip(2).join(Joiner.on("\n"));
        Assert.fail("Diff:\n" + result);
    }

    private List<String> sanitize(List<String> strings) {
        final Pattern millis = Pattern.compile("\\s*\"millis\"\\s*:.*");
        final Pattern correlationId = Pattern.compile("\\s*\"correlationId\"\\s*:.*");
        final Pattern systemId = Pattern.compile("\\s*\"systemId\"\\s*:.*");

        return FluentIterable.from(strings).filter(new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return !(millis.matcher(s).matches()
                        || correlationId.matcher(s).matches()
                        || systemId.matcher(s).matches());
            }
        }).toList();
    }

    private List<String> toLines(String json) throws IOException {
        return IOUtils.readLines(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    private String original(FrameworkMethod method) throws IOException {
        CompareTo annotation = method.getAnnotation(CompareTo.class);
        if (annotation == null) throw new RuntimeException("Annotation CompareTo not found");

        return IOUtils.toString(this.getClass().getResourceAsStream(annotation.value()));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface CompareTo {
        String value();
    }
}
