package httpc.impl;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import httpc.api.Handler;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class HttpHandler extends HttpClient implements Handler {
    @Override
    public Response handleResponseFromGET(Request request, Response response) {
        try{
            switch(response.getStatusCode()) {
                case "300":
                    if(!StringUtils.isEmpty(response.getHeaders().get("Location"))){
                        request.setRedirectUrl(new URL(response.getHeaders().get("Location")));
                        request.setUrl(new URL(response.getHeaders().get("Location")));
                        return get(request);
                    }
                case "301":
                    if(!StringUtils.isEmpty(response.getHeaders().get("Location"))){
                        request.setRedirectUrl(new URL(response.getHeaders().get("Location")));
                        request.setUrl(new URL(response.getHeaders().get("Location")));
                        return get(request);
                    }
                case "302":
                    //TODO
                case "304":
                    //TODO
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("Redirect url is not an absolute path");
        }
        return response;
    }

    @Override
    public Response handleResponseFromPOST(Request request, Response response) {
        return null;
    }
}