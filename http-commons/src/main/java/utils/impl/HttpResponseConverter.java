package utils.impl;

import RequestAndResponse.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.api.Converter;

import java.util.*;

public class HttpResponseConverter implements Converter<Response, String> {

    public Response convert(String s) {

        Response response = new Response();

        String str = s.replaceAll("\r\n", "\n"); //Used become some servers returns responses using either "\r\n" OR "\n" only.
        String[] res = str.split("\n\n");

        if(res.length == 2){ /* With body*/
            if(getNumberOfLineFeed(res[0]) == 0){ /* Without headers */
                populateResponseVersionStatusCodePhrase(res[0], response);
                populateBody(res[1],response);
            } else {/* With at least 1 header */
                String[] resAndHeaders = res[0].split("\n");
                populateResponseVersionStatusCodePhrase(resAndHeaders[0], response);
                populateResponseHeaders(new HashMap<String, String>(), resAndHeaders, response);
                populateBody(res[1],response);
            }
        } else { /* Without body */
            String[] resAndHeaders = res[0].split("\n");
            if(resAndHeaders.length == 1){ /*Without headers*/
                populateResponseVersionStatusCodePhrase(resAndHeaders[0], response);
            } else { /* With at least 1 header */
                populateResponseVersionStatusCodePhrase(resAndHeaders[0], response);
                populateResponseHeaders(new HashMap<String, String>(), resAndHeaders, response);
            }

        }
        return response;
    }

    /* First index is version then status code then phrase */
    private void populateResponseVersionStatusCodePhrase(String resAndHeader, @NotNull Response response){
        String[] versionStatusCodePhrase = resAndHeader.split(" ");
        response.setVersion(versionStatusCodePhrase[0]);
        response.setStatusCode(versionStatusCodePhrase[1]);
        response.setPhrase(versionStatusCodePhrase[2]);
        if (versionStatusCodePhrase.length == 4) {
            response.setPhrase(versionStatusCodePhrase[2] + " " + versionStatusCodePhrase[3]);
        } else if (versionStatusCodePhrase.length == 5){
            response.setPhrase(versionStatusCodePhrase[2] + " " + versionStatusCodePhrase[3] + " " + versionStatusCodePhrase[4]);
        }
    }

    /* Everything in between ( [1... endOfArray[ ) are headers */
    private void populateResponseHeaders(Map<String, String> headers,String[] headerStringArray, @NotNull Response response) {
        List<String> headerStringList = new LinkedList<String> (Arrays.asList(headerStringArray));
        headerStringList.remove(0); //removing first element as it is not a header

        for(String headerString : headerStringList){
            String[] keyvalue = headerString.split(":");
            String headerName = keyvalue[0];
            String headerValue = keyvalue[1];

            headers.put(headerName, headerValue);
        }
        response.setHeaders(headers);
    }

    /* Last index of res array [endOfArray] is body */
    private void populateBody(String res, @NotNull Response response) {
        response.setBody(res);
    }

    private int getNumberOfLineFeed(String s) {
        return StringUtils.countMatches(s, "\n");
    }
}
