package utils.impl;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import RequestAndResponse.Response;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import utils.api.Parser;

import java.io.InputStream;
import java.util.HashMap;

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

        Method method = request.getHttpMethod();
        StringBuilder requestToSend = new StringBuilder();

        /* Request line */
        requestToSend.append(request.getHttpMethod().toString().toUpperCase()) //Method
                .append(" ") //Sp
                .append(request.getPath());//path

        //IMPORTANT TO UNDERSTAND THE LOGIC HERE//
        /* query are put in the url for get request */
        switch(method) {
            case GET:
                if(StringUtils.isEmpty(request.getUrl().getQuery()) && !MapUtils.isEmpty(request.getQueryParameters())) { //append query to url
                    requestToSend.append("?");
                    for(String key : request.getQueryParameters().keySet()) {
                        requestToSend.append(key)
                                .append("=")
                                .append(request.getQueryParameters().get(key))
                                .append("&");
                    }
                    requestToSend.setLength(requestToSend.length() - 1 ); //removing trailing ',' character
                } else if(!StringUtils.isEmpty(request.getUrl().getQuery()) && !MapUtils.isEmpty(request.getQueryParameters())) {
                    System.out.println("You executed a GET request with query in both the url and as parameter. Query in url will prevail");
                }
                break;
            case POST:
                if(StringUtils.isEmpty(request.getUrl().getQuery()) && !MapUtils.isEmpty(request.getQueryParameters())) {
                    if(MapUtils.isEmpty(request.getHeaders())){
                        request.setHeaders(new HashMap<String, String>());
                    }
                    request.getHeaders().put("Content-Type"," application/x-www-form-urlencoded");
                    StringBuilder str = new StringBuilder();
                    for(String key : request.getQueryParameters().keySet()) {
                        str.append(key)
                                .append("=")
                                .append(request.getQueryParameters().get(key))
                                .append("&");
                    }
                    str.setLength(str.length() - 1 ); //removing trailing ',' character
                    request.setBody(str.toString()); //put query in body
                } else if (!MapUtils.isEmpty(request.getQueryParameters()) && !StringUtils.isEmpty(request.getUrl().getQuery())){
                    System.out.println("You executed a POST request with query in both the url and as parameter. Query in url will prevail");
                }
                break;
        }

        requestToSend.append(" ") //Sp
                .append(request.getVersion())
                .append("\r\n"); //version + cr + lf

        /* Request Headers */
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
