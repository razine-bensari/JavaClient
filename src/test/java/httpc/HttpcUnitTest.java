package httpc;

import RequestAndResponse.Response;
import httpc.impl.HttpExecutor;
import httpc.impl.HttpValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpcUnitTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void mixedOptionsAndParametersShouldBeFilteredForHttpcCommand() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        httpc.get(null,null,null,null, true, str);

    }

    @Test
    public void httpcCommandGetWithOneHeader() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1"};

        httpc.get(headers,null,null,null, true,str);
    }

    @Test(expected = ParsingException.class)
    public void httpcCommandGetWithOneInvalidHeaderNoDoubleDot() {
        String str = "http://httpbin.org/ip";

        HttpExecutor httpcExecutor = new HttpExecutor();

        String[] headers = {"header1NOTDOTvalue1"};

        httpcExecutor.executeGET(headers,null,null,null, str);
    }

    @Test(expected = ParsingException.class)
    public void httpcCommandGetWithOneInvalidHeaderWith3DoubleDot() {
        String str = "http://httpbin.org/ip";

        HttpExecutor httpcExecutor = new HttpExecutor();

        String[] headers = {"header1NOTDOT:va:lu:e1"};

        httpcExecutor.executeGET(headers,null,null,null, str);
    }

    @Test
    public void httpcCommandGetWithThreeHeader() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        httpc.get(headers,null,null,null, true,str);

    }

    @Test
    public void httpcCommandGetWithQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1"};

        httpc.get(headers,null,query,null, true,str);
    }

    @Test(expected = ParsingException.class)
    public void httpcCommandGetWithInvalidQuery() {
        String str = "http://httpbin.org/ip";

        HttpExecutor httpcExecutor = new HttpExecutor();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1NOEQUEALSSIGNSvalue1"};

        httpcExecutor.executeGET(headers,null,query,null, str);

    }

    @Test(expected = ParsingException.class)
    public void httpcCommandGetWithInvalidQueryWith3EqualSign() {
        String str = "http://httpbin.org/ip";

        HttpExecutor httpcExecutor = new HttpExecutor();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1NOE=QUEAL=SSIGN=Svalue1"};

        httpcExecutor.executeGET(headers,null,query,null,str);
    }

    @Test
    public void httpcCommandPostWithQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1"};

        httpc.post(null,  null, headers,null, query, null, true,str);

    }

    @Test
    public void httpcCommandPostWithBody() {
        String str = "http://httpbin.org/post";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        httpc.post("{ THIS IS MY BODY :) }",  null, headers,null, null, null, true,str);
    }

    @Test
    public void httpcCommandPostWithSeveralQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  null, headers,null, query, null, true,str);

    }

    @Test
    public void httpcCommandPostWithSeveralQueryAndUrlQuery() {
        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  null, headers,null, query, null, true,str);

    }

    @Test(expected = ParsingException.class)
    public void httpcCommandPostWithFileAndBody() {
        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        HttpValidator validator = new HttpValidator();

        String filename = "file.txt";

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        validator.validatePostRequest("thisissbody",  filename, headers,null, query, null, str);
    }

    @Test
    public void httpcCommandPostWithFileBody() {

        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        Httpc httpc = new Httpc();

        String filename = "bodysample.txt";

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  filename, headers,null, query, null, true, str);
    }

    @Test
    public void httpcCommandOutputResponseTofile() {
        String fileName = "response.txt";
        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        Httpc httpc = new Httpc();

        httpc.post(null,  null, null,fileName, null, null, true,str);

        File file = Paths.get(fileName).toFile();

        assert file.exists();
    }

    @Test
    public void httpCommandWithREdirectResponse() {
        Response response = new Response();
        Map<String, String> headers = new HashMap<String, String>();
                headers.put("Location", "http://httpbin.org/ip");
        response.setHeaders(headers);
        response.setStatusCode("300");

        String str = "http://httpbin.org/";

        Httpc httpc = new Httpc();

        String[] header = {"header1:header1"};

        httpc.get(header,null,null,null, true,str);

    }

    @Test
    public void temp() {
        //
    }
}

