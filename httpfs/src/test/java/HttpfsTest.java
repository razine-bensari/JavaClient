import httpc.Httpc;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;

public class HttpfsTest {

    private HttpRequestConverter converter;
    private HttpParser parser;

    @BeforeEach
    public void beforeEach(){
    }

    @Test
    public void testconnectionServer() {

            Httpc httpc = new Httpc();

//        Httpfs server = new Httpfs(8080);
//
//        server.run();
        //String[] headers = {"Content-Type:text/html"};

        httpc.get(null ,null,null,null,false, "http://localhost:8080/ip");

    }
}
