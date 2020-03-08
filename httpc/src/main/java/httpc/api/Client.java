package httpc.api;

import RequestAndResponse.Request;
import RequestAndResponse.Response;

public interface Client {
    Response get(Request request);
    Response post(Request request);
}
