package httpc.api;

import RequestAndResponse.Request;
import RequestAndResponse.Response;

public interface Handler {
    Response handleResponseFromGET(Request request, Response response);
    Response handleResponseFromPOST(Request request, Response response);
}
