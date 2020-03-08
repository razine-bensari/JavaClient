package httpc;

import RequestAndResponse.Response;
import httpc.Httpc;
import org.junit.jupiter.api.Test;
import utils.impl.HttpParser;

import static org.junit.Assert.assertEquals;

public class HttpHandlerUnitTest {

    @Test
    public void httpHandlerWithRedirectResponse() {
        try {
            HttpParser parser = new HttpParser();

            String str = "http://httpbin.org/absolute-redirect/3";

            Httpc httpc = new Httpc();

            String[] header = {"Accept: text/html"};

            String[] param = {"url=http://httpbin.org"};

            Response res = httpc.get(header,null,param,null, false,str);

            System.out.println(parser.parseResponse(res));
            assertEquals("302", res.getStatusCode());

        } catch (Exception e) {
           System.out.println(e.getMessage());
        }
    }
}
