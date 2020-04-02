package httpfs;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import utils.api.Converter;
import utils.api.Parser;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestWorker {

    private String dirpath;
    private Parser parser = new HttpParser();
    private Converter<Request, String> converter = new HttpRequestConverter();

    public RequestWorker(String dirpath) {
        this.dirpath = dirpath;
    }

    public Response processRequest(Request request) {
        try{
            switch(request.getHttpMethod()) {
                case GET:
                    if (request.getPath().equals("/")) {
                        return buildListOfFile(request);
                    } else {
                        return getFile(request);
                    }
                case POST:
                    return persistFile(request);
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
                    headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length+1));
                    response.setHeaders(headers);
                    return response;
            }

        } catch (Exception e){
            e.getMessage();
            Response response = new Response();
            response.setPhrase("Internal Server Error");
            response.setStatusCode("500");
            response.setVersion("HTTP/1.0");
            return response;
        }
    }

    private Response buildListOfFile(Request request) {
        Response response = new Response();
        response.setVersion("HTTP/1.0");
        Map<String, String> headers = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String absolutePath = "/Users/razine/workspace/JavaClientServerHTTP/fs";
        String pathFile;
        try {
            List<String> listOfFileNames = new ArrayList<>();
            if(!StringUtils.isEmpty(dirpath)){
                pathFile = absolutePath + dirpath + request.getPath();
            } else {
                pathFile = absolutePath + request.getPath();
            }
            final List<Path> collect = Files.walk(Paths.get(pathFile))
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
            } else if (request.getPath().contains("..")){
                response.setStatusCode("400");
                headers.put("Content-Type", "text/plain");
                response.setHeaders(headers);
                response.setBody("Path for file is not valid");
                response.setPhrase("Bad Request");
                return response;
            } else {
                response.setPhrase("OK");
                response.setStatusCode("200");
            }
            response.setBody(sb.toString());
            headers.put("Content-Type", "text/plain");
            headers.put("Content-Length", String.valueOf(sb.toString().getBytes(StandardCharsets.UTF_8).length+1));
            response.setHeaders(headers);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
        return  response;
    }

    private Response getFile(Request request) throws IOException {
        Response response = new Response();
        response.setVersion("HTTP/1.0");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");

        String absolutePath = "/Users/razine/workspace/JavaClientServerHTTP/fs";
        String pathFile;
        if(!StringUtils.isEmpty(dirpath)){
            pathFile = absolutePath + dirpath + request.getPath();
        } else {
            pathFile = absolutePath + request.getPath();
        }
        File file = new File(pathFile);
        if(pathFile.contains("..") || !getfileName(request.getPath()).contains(".")) {
            response.setStatusCode("400");
            response.setHeaders(headers);
            response.setBody("Path for file is not valid");
            response.setPhrase("Bad Request");
            return response;
        } else if (file.exists() && !Files.isWritable(file.toPath())) {
            response.setStatusCode("403");
            response.setHeaders(headers);
            response.setBody("The requested file is not readable");
            response.setPhrase("Forbidden");
            return response;
        } else if (file.exists() && Files.isWritable(file.toPath())) {
            response.setStatusCode("200");
            response.setPhrase("OK");
            response.setBody(FileUtils.readFileToString(file, "UTF-8"));
            headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length+1));
            response.setHeaders(headers);
            return response;
        } else {
            response.setStatusCode("404");
            response.setPhrase("Not Found");
            response.setBody("The requested file has not been found");
            headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length+1));
            response.setHeaders(headers);
            return response;
        }
    }

    public Response persistFile(Request request) throws IOException {
        Response response = new Response();
        response.setVersion("HTTP/1.0");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("overwrite", "false");

        String absolutePath = "/Users/razine/workspace/JavaClientServerHTTP/fs";
        String pathFile;
        if(!StringUtils.isEmpty(dirpath)){
           pathFile = absolutePath + dirpath + request.getPath();
        } else {
            pathFile = absolutePath + request.getPath();
        }
        File file = new File(pathFile);
        if(pathFile.contains("..") || !getfileName(request.getPath()).contains(".")) {
            response.setStatusCode("400");
            response.setHeaders(headers);
            response.setBody("Path for file is not valid");
            response.setPhrase("Bad Request");
            return response;
        } else if(!file.exists() && getfileName(request.getPath()).contains(".")) {
            FileOutputStream f = FileUtils.openOutputStream(file);
            f.write(request.getBody().getBytes());
            response.setStatusCode("201");
            response.setPhrase("Created");
            f.close();
        } else if(file.exists() && Files.isWritable(file.toPath()) && getfileName(request.getPath()).contains(".")) {
            FileOutputStream f = FileUtils.openOutputStream(file);
            f.write(request.getBody().getBytes());
            headers.put("overwrite", "true");
            response.setStatusCode("201");
            response.setPhrase("Created");
            f.close();
        } else if (file.exists() && !Files.isWritable(file.toPath())) {
            response.setStatusCode("403");
            response.setPhrase("Forbidden");
            response.setBody("you do not have the right to write to this file");
            headers.put("Content-Length", String.valueOf(response.getBody().getBytes(StandardCharsets.UTF_8).length+1));
            response.setHeaders(headers);
            return response;
        }
        response.setHeaders(headers);
        return response;
    }

    public String getfileName(String path) {
        String[] dirsAndFileName = path.split("/");
        return dirsAndFileName[dirsAndFileName.length-1];
    }
}
