import com.jmisur.Foo;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncTest {

    @Rule
    public CaptureRule capture = new CaptureRule();

    @Test
    @CaptureRule.CompareTo("async-testExecutor.json")
    public void testExecutor() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Foo.main(new String[0]);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        });
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    @CaptureRule.CompareTo("async-testRunnable.json")
    public void testRunnable() throws IOException, InterruptedException {
        Foo.main(new String[0]);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        });
        t.setName("Thread-1");
        t.start();
        t.join();
    }

    @Test
    @CaptureRule.CompareTo("async-testThread.json")
    public void testThread() throws IOException, InterruptedException {
        Foo.main(new String[0]);
        Thread t = new Thread() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        };
        t.setName("Thread-1");
        t.start();
        t.join();
    }
}
