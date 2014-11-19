import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

public class SocketTest {
    @Rule
    public CaptureRule capture = new CaptureRule(true);

    private Server server;

    @Before
    public void initServer() throws Exception {
        server = new Server(9998);
        server.setHandler(new TestHandler());
        server.start();
    }

    @After
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    @CaptureRule.CompareTo("socket.json")
    public void testSocket() throws IOException, URISyntaxException {
        URLConnection conn = new URI("http://localhost:9998").toURL().openConnection();
//        URLConnection conn = new URI("http://www.google.com").toURL().openConnection();

        try (InputStream is = conn.getInputStream()) {
            IOUtils.toString(is);
        }
    }

    static class TestHandler extends AbstractHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            try (InputStream is = getClass().getResourceAsStream("socket-response.json")) {
                IOUtils.copy(is, response.getOutputStream());
            }
            baseRequest.setHandled(true);
        }
    }
}
