package httpc;

import org.junit.jupiter.api.Test;

public class HttpcUnitTest {

    @Test
    public void mixedOptionsAndParametersShouldBeFilteredForHttpcCommand() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        httpc.get(null,null,null,null, str);



    }

    @Test
    public void httpcCommandGetWithOneHeader() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1"};

        httpc.get(headers,null,null,null, str);
    }

    @Test
    public void httpcCommandGetWithThreeHeader() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        httpc.get(headers,null,null,null, str);

    }

    @Test
    public void httpcCommandGetWithQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1"};

        httpc.get(headers,null,query,null, str);

    }

    @Test
    public void httpcCommandPostWithQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1"};

        httpc.post(null,  null, headers,null, query, null, str);

    }

    @Test
    public void httpcCommandPostWithSeveralQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  null, headers,null, query, null, str);

    }

    @Test
    public void httpcCommandPostWithSeveralQueryAndUrlQuery() {
        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  null, headers,null, query, null, str);

    }
}

