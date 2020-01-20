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
        String request1;
        return request1 = "GET /status/999 HTTP/1.0\r\n\r\n";
    }
}
