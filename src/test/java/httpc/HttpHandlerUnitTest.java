package httpc;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import httpc.api.Handler;
import httpc.impl.HttpHandler;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpHandlerUnitTest {

    @Test
    public void httpHandlerWithRedirectResponse() {
        try {

            Handler handler = new HttpHandler();
            Response response = new Response();
            Map<String, String> headers = new HashMap<String, String>();

            headers.put("Location", "http://httpbin.org/ip");
            response.setHeaders(headers);
            response.setStatusCode("300");

            String str = "http://httpbin.org/";

            Request request = new Request.Builder()
                    .withVersion("HTTP/1.0")
                    .withHttpMethod(Method.GET)
                    .withBody("bodyofrequest")
                    .withUrl(new URL("http://httpbin.org"))
                    .Build();

            Response res = handler.handleResponseFromGET(request, response);

            assertEquals(res.getStatusCode(), "400");

        } catch (Exception e) {
           System.out.println(e.getMessage());
        }
    }
}
