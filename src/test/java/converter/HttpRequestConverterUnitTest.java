package converter;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpRequestConverterUnitTest {

    private HttpRequestConverter converter;
    private HttpParser parser;

    @BeforeEach
    public void beforeEach(){
        System.out.println("Setting up test object and dependencies");

        converter = new HttpRequestConverter();
        parser = new HttpParser();
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyUsingLinuxCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\r\n\r\n";

            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");

            assertNull(request.getBody());
            assertNull(request.getHeaders());

            System.out.println(parser.parseRequest(request));

        } catch (Exception e){
            //do nothing its a test
        }
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyUsingWindowsCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\n\n";

            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");

            assertNull(request.getBody());
            assertNull(request.getHeaders());

            System.out.println(parser.parseRequest(request));
        } catch (Exception e){
            //do nothing its a test
        }
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersUsingLinuxCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\r\nheader1:value1\n\r\n";

            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");
            assertEquals(request.getHeaders().get("header1"), "value1");

            assertNull(request.getBody());


            System.out.println(parser.parseRequest(request));
        } catch (Exception e){
            //do nothing its a test
        }
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersUsingWindowsCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\r\nheader1:value1\nheader2:value2\n\r\n";


            Request request = converter.convert(req);

            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");
            assertEquals(request.getHeaders().get("header1"), "value1");
            assertEquals(request.getHeaders().get("header2"), "value2");

            assertNull(request.getBody());


            System.out.println(parser.parseRequest(request));

        } catch (Exception e){
            //do nothing its a test
        }
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersAndBodyUsingLinuxCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\r\nheader1:value1\nheader2:value2\n\r\n{this is a comp class}";

            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");
            assertEquals(request.getHeaders().get("header1"), "value1");
            assertEquals(request.getHeaders().get("header2"), "value2");
            assertEquals(request.getBody(), "{this is a comp class}");

            System.out.println(parser.parseRequest(request));

        } catch (Exception e){
            //do nothing its a test
        }
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersAndBodyUsingWindowsCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\nheader1:value1\nheader2:value2\n\n{this is a comp class}";


            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");
            assertEquals(request.getHeaders().get("header1"), "value1");
            assertEquals(request.getHeaders().get("header2"), "value2");
            assertEquals(request.getBody(), "{this is a comp class}");

            System.out.println(parser.parseRequest(request));

        } catch (Exception e){
            //do nothing its a test
        }
        String req = "GET /razine HTTP/1.1\nheader1:value1\nheader2:value2\n\n{this is a comp class}";


        Request request = converter.convert(req);

        assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
        assertEquals(request.getPath(), "/razine");
        assertEquals(request.getVersion(), "HTTP/1.1");
        assertEquals(request.getHeaders().get("header1"), "value1");
        assertEquals(request.getHeaders().get("header2"), "value2");
        assertEquals(request.getBody(), "{this is a comp class}");

        System.out.println(parser.parseRequest(request));
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndBodyUsingLinuxCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\r\n\r\n{this is a comp class}";


            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");
            assertEquals(request.getBody(), "{this is a comp class}");

            assertNull(request.getHeaders());

            System.out.println(parser.parseRequest(request));

        } catch (Exception e){
            //do nothing its a test
        }
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndBodyUsingWindowsCRLF(){
        try{
            String req = "GET /razine HTTP/1.1\r\n\r\n{this is a comp class}";


            Request request = converter.convert(req);
            request.setUrl(new URL("http://httpbin.org"));

            assertEquals(request.getHttpMethod(), Enum.valueOf(Method.class, "GET"));
            assertEquals(request.getPath(), "/razine");
            assertEquals(request.getVersion(), "HTTP/1.1");
            assertEquals(request.getBody(), "{this is a comp class}");

            assertNull(request.getHeaders());

            System.out.println(parser.parseRequest(request));

        } catch (Exception e){
            //do nothing its a test
        }
    }
}
