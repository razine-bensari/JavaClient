package utils.api;

import RequestAndResponse.Request;
import RequestAndResponse.Response;

import java.io.InputStream;

public interface Parser {
    String parseResponse(Response response);
    String parseResponse(InputStream inputStream);
    String parseRequest(Request request);
}
