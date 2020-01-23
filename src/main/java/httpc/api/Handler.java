package httpc.api;

import RequestAndResponse.Response;

public interface Handler {
    Response handleResponseFromGET(Response response);
    Response handleResponseFromPOST(Response response);
}
