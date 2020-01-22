package utils.impl;

import RequestAndResponse.Response;
import utils.api.Converter;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseConverter implements Converter<Response, String> {

    public Response convert(String s) {

        Response response = new Response();

        String str = s.replaceAll("\r\n", "\n"); //Used become some servers returns responses using either "\r\n" OR "\n" only.
        String[] res = str.split("\n");

        //First index is version, status code and phrase
        String[] versionStatusCodePhrase = res[0].split(" ");
        response.setVersion(versionStatusCodePhrase[0]);
        response.setStatusCode(versionStatusCodePhrase[1]);
        response.setPhrase(versionStatusCodePhrase[2]);

        //Last index is my body
        String body = res[res.length -1];
        response.setBody(body);

        //Everything in between are headers
        Map<String, String> headers = new HashMap<String, String>();
        for(int i = 1; i <= res.length -2; i++){
            String[] keyvalue = res[i].split(":");
            String headerName = keyvalue[0];
            String headerValue = keyvalue[1];

            headers.put(headerName, headerValue);
        }
        response.setHeaders(headers);

        return response;

    }
}
