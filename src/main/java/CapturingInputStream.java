import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CapturingInputStream extends InputStream {
    private InputStream target;
    private ByteBuffer buffer = ByteBuffer.allocate(65536);

    private CapturingInputStream(InputStream target) {
        this.target = target;
    }

    public static InputStream wrap(InputStream target) {
        return new CapturingInputStream(target);
    }

    @Override
    public int read() throws IOException {
        int res = target.read();
        if (canWrite(res)) buffer.putInt(res);
        return res;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int res = target.read(b);
        if (canWrite(res)) buffer.put(b, 0, maxOf(res));
        return res;
    }

    private boolean canWrite(int res) {
        return res >= 0 && buffer.hasRemaining();
    }

    private int maxOf(int res) {
        return res > buffer.remaining() ? buffer.remaining() : res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = target.read(b, off, len);
        if (canWrite(res)) buffer.put(b, off, maxOf(res));
        return res;
    }

    @Override
    public long skip(long n) throws IOException {
        return target.skip(n);
    }

    @Override
    public int available() throws IOException {
        return target.available();
    }

    @Override
    public void close() throws IOException {
        target.close();
        System.out.println("CAPTURED: " + new String(buffer.array()));
    }

    @Override
    public void mark(int readlimit) {
        target.mark(readlimit); // TODO
    }

    @Override
    public void reset() throws IOException {
        target.reset(); // TODO
    }

    @Override
    public boolean markSupported() {
        return target.markSupported();
    }
}
