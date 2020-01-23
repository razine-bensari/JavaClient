package httpc.impl;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import httpc.api.Client;
import httpc.api.Executor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.impl.HttpHeaderConverter;
import utils.impl.HttpQueryConverter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpExecutor implements Executor {

    public Client client = new HttpClient();
    public HttpHeaderConverter headerConverter = new HttpHeaderConverter();
    public HttpQueryConverter queryConverter = new HttpQueryConverter();

    public Response executePOST(String body, String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, @NotNull String urlfromCLI) {
        try {
            Request request = new Request.Builder(urlfromCLI)
                    .withHttpMethod(Method.POST)
                    .withVersion("HTTP/1.0")
                    .Build();
            if(!ArrayUtils.isEmpty(headersFromCLI)) {
                request.setHeaders(headerConverter.convert(headersFromCLI));
            }
            if(!StringUtils.isEmpty(fileName)) {
                request.setFile(new File(fileName));
            }
            if(!StringUtils.isEmpty(redirectUrlFromCLI)) {
                request.setRedirectUrl(new URL(redirectUrlFromCLI));
                //TODO add logic to add header for redirect value
            }
            if(!ArrayUtils.isEmpty(queryFromCLI)) {
                request.setQueryParameters(queryConverter.convert(queryFromCLI));
            }
            if(!StringUtils.isEmpty(body)) {
                request.setBody(body);
            }
            return client.post(request);
        } catch (MalformedURLException e){
            System.out.printf("%s", e.getMessage());
        }
        return new Response(); //empty response if exception is thrown
    }

    public Response executeGET(String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, @NotNull String urlfromCLI) {
        try {
            Request request = new Request.Builder(urlfromCLI)
                    .withHttpMethod(Method.GET)
                    .withVersion("HTTP/1.0")
                    .Build();
            if(!ArrayUtils.isEmpty(headersFromCLI)) {
                request.setHeaders(headerConverter.convert(headersFromCLI));
            }
            if(!StringUtils.isEmpty(fileName)) {
                request.setFile(new File(fileName));
            }
            if(!StringUtils.isEmpty(redirectUrlFromCLI)) {
                request.setRedirectUrl(new URL(redirectUrlFromCLI));
                //TODO add logic to add header for redirect value
            }
            if(!ArrayUtils.isEmpty(queryFromCLI)) {
                request.setQueryParameters(queryConverter.convert(queryFromCLI));
                //TODO add logic to transfer url-based query to the query attribute
            }
            return client.get(request);
        } catch (MalformedURLException e){
            System.out.printf("%s", e.getMessage());
        }
        return new Response(); //empty response if exception is thrown
    }
}
