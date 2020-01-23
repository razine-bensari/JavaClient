package httpc.impl;

import RequestAndResponse.Response;
import httpc.api.Handler;

public class HttpHandler implements Handler {
    @Override
    public Response handleResponseFromGET(Response response) {
        switch(response.getStatusCode()) {
            case "300":
                //TODO
            case "301":
                //TODO
            case "302":
                //TODO
            case "304":
                //TODO
        }
        return null;
    }

    @Override
    public Response handleResponseFromPOST(Response response) {
        return null;
    }
}