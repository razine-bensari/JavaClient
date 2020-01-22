package utils.impl;

import RequestAndResponse.Response;
import utils.api.Converter;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseConverter implements Converter<Response, String> {

    public Response convert(String s) {

        final Response response = new Response();

        String str = s.replaceAll("\r\n", "\n"); //Used become some servers returns responses using either "\r\n" OR "\n" only.
        String[] res = str.split("\n");

        int level = res.length;
        switch (level) {
            case 2:
                populateResponseVersionStatusCodePhrase(res, response);
                populateResponseHeaders(new HashMap<String, String>(), res, response);
            case 3:
                populateResponseVersionStatusCodePhrase(res, response);
                populateResponseHeaders(new HashMap<String, String>(), res, response);
                populateBody(res, response);
            default:
                populateResponseVersionStatusCodePhrase(res, response);
        }
        return response;
    }

    //First index is version, status code and phrase [0]
    private void populateResponseVersionStatusCodePhrase(String[] res, Response response){
        //First index is version, status code and phrase
        String[] versionStatusCodePhrase = res[0].split(" ");
        response.setVersion(versionStatusCodePhrase[0]);
        response.setStatusCode(versionStatusCodePhrase[1]);
        response.setPhrase(versionStatusCodePhrase[2]);
    }

    //Everything in between ( [1... endOfArray[ ) are headers
    private void populateResponseHeaders(Map<String, String> headers,String[] res, Response response) {
        for(int i = 1; i <= res.length -2; i++){
            String[] keyvalue = res[i].split(":");
            String headerName = keyvalue[0];
            String headerValue = keyvalue[1];

            headers.put(headerName, headerValue);
        }
        response.setHeaders(headers);
    }

    //Last index of res array [endOfArray] is body
    private void populateBody(String[] res, Response response) {
        String body = res[res.length -1];
        response.setBody(body);
    }
}
