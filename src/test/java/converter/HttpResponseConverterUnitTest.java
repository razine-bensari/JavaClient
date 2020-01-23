package converter;

import RequestAndResponse.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.impl.HttpParser;
import utils.impl.HttpResponseConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpResponseConverterUnitTest {

    private HttpResponseConverter converter;
    private HttpParser parser;

    @BeforeEach
    public void beforeEach(){
        System.out.println("Setting up test object and dependencies");

        converter = new HttpResponseConverter();
        parser = new HttpParser();
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyUsingLinuxCRLF(){
        String res = "HTTP/1.1 200 OK\r\n\r\n";

        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");

        assertNull(response.getBody());
        assertNull(response.getHeaders());

        System.out.println(parser.parseResponse(response));
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyUsingWindowsCRLF(){
        String res = "HTTP/1.1 200 OK\n\n";

        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");

        assertNull(response.getBody());
        assertNull(response.getHeaders());

        System.out.println(parser.parseResponse(response));
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersUsingLinuxCRLF(){
        String res = "HTTP/1.1 200 OK\r\nheader1:value1\r\n\r\n";

        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");
        assertEquals(response.getHeaders().get("header1"), "value1");
        assertNull(response.getBody());

        System.out.println(parser.parseResponse(response));

    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersUsingWindowsCRLF(){
        String res = "HTTP/1.1 200 OK\nheader1:value1\r\nheader2:value2\n\n";


        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");
        assertEquals(response.getHeaders().get("header1"), "value1");
        assertEquals(response.getHeaders().get("header2"), "value2");
        assertNull(response.getBody());

        System.out.println(parser.parseResponse(response));

    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersAndBodyUsingLinuxCRLF(){
        String res = "HTTP/1.1 200 OK\r\nheader1:value1\r\n\r\n{this a body, COMP 445}";

        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");
        assertEquals(response.getHeaders().get("header1"), "value1");
        assertEquals(response.getBody(), "{this a body, COMP 445}");
        System.out.println(parser.parseResponse(response));
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndHeadersAndBodyUsingWindowsCRLF(){
        String res = "HTTP/1.1 200 OK\nheader1:value1\nheader2:value2\n\n{this is body, COMP 445}";


        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");
        assertEquals(response.getHeaders().get("header1"), "value1");
        assertEquals(response.getHeaders().get("header2"), "value2");
        assertEquals(response.getBody(), "{this is body, COMP 445}");

        System.out.println(parser.parseResponse(response));
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndBodyUsingLinuxCRLF(){
        String res = "HTTP/1.1 200 OK\r\n\r\n{this a body, COMP 445}";

        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");
        assertNull(response.getHeaders());
        assertEquals(response.getBody(), "{this a body, COMP 445}");

        System.out.println(parser.parseResponse(response));
    }

    @Test
    public void responseWithVersionStatusCodePhraseOnlyAndBodyUsingWindowsCRLF(){
        String res = "HTTP/1.1 200 OK\n\n{this a body, COMP 445}";

        Response response = converter.convert(res);

        assertEquals(response.getPhrase(), "OK");
        assertEquals(response.getStatusCode(), "200");
        assertEquals(response.getVersion(), "HTTP/1.1");
        assertNull(response.getHeaders());
        assertEquals(response.getBody(), "{this a body, COMP 445}");

        System.out.println(parser.parseResponse(response));
    }
}
