package httpc.impl;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import httpc.ParsingException;
import httpc.api.Client;
import httpc.api.Executor;
import httpc.api.Handler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.api.Parser;
import utils.impl.HttpHeaderConverter;
import utils.impl.HttpParser;
import utils.impl.HttpQueryConverter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HttpExecutor implements Executor {

    public Client client = new HttpClient();
    public HttpHeaderConverter headerConverter = new HttpHeaderConverter();
    public HttpQueryConverter queryConverter = new HttpQueryConverter();
    public Parser parser = new HttpParser();
    public Handler handler = new HttpHandler();

    public Response executePOST(String body, String fileBody, String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, @NotNull String urlfromCLI) {
        try {
            Request request = new Request.Builder(urlfromCLI)
                    .withHttpMethod(Method.POST)
                    .withVersion("HTTP/1.0")
                    .Build();
            if(!ArrayUtils.isEmpty(headersFromCLI)) {
                request.setHeaders(headerConverter.convert(headersFromCLI));
                request.getHeaders().put("Content-Length", getContentLength(body));
            }
            if(!StringUtils.isEmpty(fileName)) {
                request.setFile(new File(fileName));
            }
            if(!StringUtils.isEmpty(redirectUrlFromCLI)) {
                request.setRedirectUrl(new URL(redirectUrlFromCLI));
                //TODO add logic to add header for redirect value
            }
            if(!StringUtils.isEmpty(body)) {
                request.setBody(body);
            }
            if(!ArrayUtils.isEmpty(queryFromCLI)) { /* Will overwrite body if the*/
                request.setQueryParameters(queryConverter.convert(queryFromCLI));
            }
            if(!StringUtils.isEmpty(fileBody)){
                ArrayList<String> linesFromFile = (ArrayList<String>) Files.readAllLines(Paths.get(fileBody), StandardCharsets.UTF_8);
                StringBuilder str = new StringBuilder();
                for (String line : linesFromFile) {
                    str.append(line);
                }
                String testFromFile = str.toString();
                request.setBody(testFromFile);
                request.getHeaders().put("Content-Length", getContentLength(request.getBody()));
            }
            if(!StringUtils.isEmpty(fileName)){
                Response response = client.post(request);
                Files.write(Paths.get(fileName), parser.parseResponse(response).getBytes());
                return response;
            }
            return handler.handleResponseFromPOST(request, client.post(request));
        } catch (Exception e){
            System.out.printf("%s", e.getMessage());
        }
        throw new ParsingException("Invalid response from server :(");
    }

    private String getContentLength(String body) throws UnsupportedEncodingException {
        if(!StringUtils.isEmpty(body)) {
            return (String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
        } else {
            return "0";
        }
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
            }
            if(!ArrayUtils.isEmpty(queryFromCLI)) {
                request.setQueryParameters(queryConverter.convert(queryFromCLI));
            }
            if(!StringUtils.isEmpty(fileName)){
                Response response = client.get(request);
                Files.write(Paths.get(fileName), parser.parseResponse(response).getBytes());
                return response;
            }
            return handler.handleResponseFromGET(request, client.get(request));
        } catch (Exception e){
            System.out.printf("%s", e.getMessage());
        }
        throw new ParsingException("Invalid response from server :(");
    }
}
