package utils.impl;

import RequestAndResponse.Request;
import utils.api.Parser;

import java.io.InputStream;

public class HttpParser implements Parser {

    public HttpParser(){
    }

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

    public String parseRequest(Request request) {
        //Do validation of fields here, such as if headers blablabla, if query blablabla in the form of Preconditions.check(a,b);
        StringBuilder requestToSend = new StringBuilder();

        //Request line
        requestToSend.append(request.getHttpMethod().toString().toUpperCase()) //Method
                .append(" ") //Sp
                .append(request.getUrl().getPath()) //URL
                .append(" ") //Sp
                .append("HTTP/1.1").append("\r\n"); //version + cr + lf

        //Request headers
        if(!request.getHeaders().isEmpty()){
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
        if(!request.getBody().isEmpty()){
            requestToSend.append(request.getBody());
        }
        return requestToSend.toString();
    }
}
