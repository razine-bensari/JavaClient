package httpfs;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import org.apache.commons.io.FileUtils;
import utils.api.Converter;
import utils.api.Parser;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestWorker implements Runnable {

    private Socket clientSocket;
    private Parser parser = new HttpParser();
    private Converter<Request, String> converter = new HttpRequestConverter();

    public RequestWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        try{
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            Response response = processRequest(converter.convert(parser.parseRequest(in)));
            out.write(parser.parseResponse(response).getBytes());
            out.flush();
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public synchronized Response processRequest(Request request) {
        try{
            switch(request.getHttpMethod()) {
                case GET:
                    if (request.getPath().equals("/")) {
                        return buildListOfFile(request);
                    } else {
                        return getFile(request);
                    }
                case POST:
                    return null;
                default:
                    Response response = new Response();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "text/plain");
                    response.setPhrase("Method Not Allowed");
                    response.setStatusCode("405");
                    response.setVersion("HTTP/1.0");
                    response.setBody("Only GET and POST request are allowed at this server");
                    headers.put("Allow", "GET, POST");
                    headers.put("Content-Type", "text/plain");
                    headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length));
                    response.setHeaders(headers);
                    return response;
            }

        } catch (Exception e){
            e.getMessage();
        }
        return null;
    }

    private synchronized Response buildListOfFile(Request request) {
        Response response = new Response();
        response.setVersion("HTTP/1.0");
        Map<String, String> headers = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        try {
            List<String> listOfFileNames = new ArrayList<>();
            final List<Path> collect = Files.walk(Paths.get("/Users/razine/workspace/JavaClientServerHTTP/fs"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for(Path path: collect) {
                listOfFileNames.add(path.toFile().getName());
            }
            for(String name: listOfFileNames) {
                sb.append(name).append("\n");
            }
            if(listOfFileNames.size() == 0){
                response.setPhrase("Not Found");
                response.setStatusCode("404");
            } else {
                response.setPhrase("OK");
                response.setStatusCode("200");
            }
            response.setBody(sb.toString());
            headers.put("Content-Type", "text/plain");
            headers.put("Content-Length", String.valueOf(sb.toString().getBytes(StandardCharsets.UTF_8).length));
            response.setHeaders(headers);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
        return  response;
    }

    private synchronized Response getFile(Request request) throws IOException {
        Response response = new Response();
        response.setVersion("HTTP/1.0");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");

        String absolutePath = "/Users/razine/workspace/JavaClientServerHTTP/fs";
        String pathFile = absolutePath + request.getPath();
        File file = new File(pathFile);
        if(file.exists() && Files.isReadable(file.toPath())) {
            response.setStatusCode("200");
            response.setPhrase("OK");
        } else if (file.exists() && !Files.isReadable(file.toPath())) {
            response.setStatusCode("403");
            response.setHeaders(headers);
            response.setBody("The requested file is not readable");
            response.setPhrase("Forbidden");
            return response;
        } else {
            response.setStatusCode("404");
            response.setPhrase("Not Found");
            response.setBody("The requested file has not been found");
            headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length + 1));
            response.setHeaders(headers);
            return response;
        }
        response.setBody(FileUtils.readFileToString(file, "UTF-8"));
        headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length + 1));
        response.setHeaders(headers);
        return response;
    }
}
