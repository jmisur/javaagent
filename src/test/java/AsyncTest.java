import com.jmisur.Foo;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncTest {
    @Test
    public void testExecutor() throws IOException, InterruptedException {
        SimpleMain.start();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Foo.main(new String[0]);
            }
        });
        Foo.main(new String[0]);
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        SimpleMain.stop();
    }
}
