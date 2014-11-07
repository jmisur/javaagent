import com.jmisur.Foo;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncTest {

//    @Rule
//    public CaptureRule capture = new CaptureRule();

    // TODO make it not depend on execution order (then sort by threadName before comparison)
    @Test
    @CaptureRule.CompareTo("asyncTest.json")
    public void testExecutor() throws IOException, InterruptedException {
//        SimpleMain.start();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Foo.main(new String[0]);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        });
        executorService.awaitTermination(1, TimeUnit.SECONDS);
//        SimpleMain.stop();
    }

    @Test
    public void testRunnable() throws IOException, InterruptedException {
        SimpleMain.start();
        Foo.main(new String[0]);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        });
        t.start();
        t.join();
        SimpleMain.stop();
    }

    @Test
    public void testThread() throws IOException, InterruptedException {
        SimpleMain.start();
        Foo.main(new String[0]);
        Thread t = new Thread() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        };
        t.start();
        t.join();
        SimpleMain.stop();
    }
}
