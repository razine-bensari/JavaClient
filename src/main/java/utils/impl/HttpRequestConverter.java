package utils.impl;

import RequestAndResponse.Method;
import RequestAndResponse.Request;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import utils.api.Converter;

import java.util.*;

public class HttpRequestConverter implements Converter<Request, String> {

    public Request convert(String s) {

        Request request = new Request.Builder().Build();

        String str = s.replaceAll("\r\n", "\n"); //Used become some servers returns responses using either "\r\n" OR "\n" only.
        String[] req = str.split("\n\n");

        if(req.length == 2){ /* With body*/
            if(getNumberOfLineFeed(req[0]) == 0){ /* Without headers */
                populateRequestMethodURLVersion(req[0], request);
                populateBody(req[1],request);
            } else {/* With at least 1 header */
                String[] resAndHeaders = req[0].split("\n");
                populateRequestMethodURLVersion(resAndHeaders[0], request);
                populateRequestHeaders(new HashMap<String, String>(), resAndHeaders, request);
                populateBody(req[1],request);
            }
        } else { /* Without body */
            String[] resAndHeaders = req[0].split("\n");
            if(resAndHeaders.length == 1){ /*Without headers*/
                populateRequestMethodURLVersion(resAndHeaders[0], request);
            } else { /* With at least 1 header */
                populateRequestMethodURLVersion(resAndHeaders[0], request);
                populateRequestHeaders(new HashMap<String, String>(), resAndHeaders, request);
            }

        }
        return request;
    }

    /* First index is Method then url then version */
    private void populateRequestMethodURLVersion(String reqAndHeaders, @NotNull Request request){
        String[] methodURLVersion = reqAndHeaders.split(" ");
        request.setHttpMethod(Enum.valueOf(Method.class, methodURLVersion[0]));
        request.setPath(methodURLVersion[1]);
        request.setVersion(methodURLVersion[2]);
    }

    /* Everything in between ( [1... endOfArray[ ) are headers */
    private void populateRequestHeaders(Map<String, String> headers, String[] headerStringArray, @NotNull Request request) {
        List<String> headerStringList = new LinkedList<String>(Arrays.asList(headerStringArray));
        headerStringList.remove(0); //removing first element as it is not a header

        for(String headerString : headerStringList){
            String[] keyvalue = headerString.split(":");
            String headerName = keyvalue[0];
            String headerValue = keyvalue[1];

            headers.put(headerName, headerValue);
        }
        request.setHeaders(headers);
    }

    /* Last index of req array [endOfArray] is body */
    private void populateBody(String res, @NotNull Request request) {
        request.setBody(res);
    }

    private int getNumberOfLineFeed(String s) {
        return StringUtils.countMatches(s, "\n");
    }
}
