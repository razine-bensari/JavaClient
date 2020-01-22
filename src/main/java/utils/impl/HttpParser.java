package utils.impl;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import utils.api.Parser;

import java.io.InputStream;

public class HttpParser implements Parser {

    public HttpParser(){
    }

    @Override
    public String parseResponse(Response response) {
        StringBuilder responseToSend = new StringBuilder();

        //Response first line
        responseToSend.append(response.getVersion()) //Method
                .append(" ") //Sp
                .append(response.getStatusCode()) //URL
                .append(" ") //Sp
                .append(response.getPhrase()).append("\r\n"); //version + cr + lf

        //Response headers
        if(!MapUtils.isEmpty(response.getHeaders())){
            for(String key : response.getHeaders().keySet()) {
                responseToSend
                        .append(key) // header field name
                        .append(":") // :
                        .append(response.getHeaders().get(key)) //value
                        .append("\r\n"); //cr and lf
            }
        }

        //cr and lf before body
        responseToSend.append("\r\n");

        //body
        if(!StringUtils.isEmpty(response.getBody())){
            responseToSend.append(response.getBody());
        }
        return responseToSend.toString();
    }

    @Override
    public String parseResponse(InputStream inputStream) {

        StringBuilder response = new StringBuilder();

        try {
            int data = inputStream.read();
            while (data != -1) {
                response.append((char) data);
                data = inputStream.read();
            }
        }catch(Exception e) {
            System.out.println("Error parsing response");
            System.out.printf("%s", e.getMessage());
        }
        return response.toString();
    }

    @Override
    public String parseRequest(Request request) {

        StringBuilder requestToSend = new StringBuilder();

        //Request line
        requestToSend.append(request.getHttpMethod().toString().toUpperCase()) //Method
                .append(" ") //Sp
                .append(request.getUrl().getPath()) //URL
                .append(" ") //Sp
                .append("HTTP/1.1").append("\r\n"); //version + cr + lf

        //Request headers
        if(!MapUtils.isEmpty(request.getHeaders())){
            for(String key : request.getHeaders().keySet()) {
                requestToSend
                        .append(key) // header field name
                        .append(":") // :
                        .append(request.getHeaders().get(key)) //value
                        .append("\r\n"); //cr and lf
            }
        }

        //cr and lf before body
        requestToSend.append("\r\n");

        //body
        if(!StringUtils.isEmpty(request.getBody())){
            requestToSend.append(request.getBody());
        }
        return requestToSend.toString();
    }
}
