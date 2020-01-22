package httpc.impl;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import httpc.api.Client;
import httpc.api.Executor;
import utils.impl.HttpHeaderConverter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpExecutor implements Executor {

    public Client client = new HttpClient();
    public HttpHeaderConverter converter = new HttpHeaderConverter();

    public Response executePOST(String body, File file, String headersFromCLI, String fileName, String queryFromCLI, String redirectUrlFromCLI, String urlfromCLI) {
        try {
            Request request = new Request.Builder(urlfromCLI)
                    .withHttpMethod(Method.post)
                    .withHeaders(converter.convert(headersFromCLI))
                    .withRedirectUrl(new URL(redirectUrlFromCLI))
                    .withBody(body)
                    .Build();
            return client.get(request);
        } catch (MalformedURLException e){
            System.out.printf("%s", e.getMessage());
        }
        return new Response(); //empty response if exception is thrown
    }

    public Response executeGET(String headersFromCLI, String fileName, String queryFromCLI, String redirectUrlFromCLI, String urlfromCLI) {
        try {
            Request request = new Request.Builder(urlfromCLI)
                    .withHttpMethod(Method.get)
                    .withHeaders(converter.convert(headersFromCLI))
                    .withRedirectUrl(new URL(redirectUrlFromCLI))
                    .Build();
            return client.get(request);
        } catch (MalformedURLException e){
            System.out.printf("%s", e.getMessage());
        }
        return new Response(); //empty response if exception is thrown
    }
}
