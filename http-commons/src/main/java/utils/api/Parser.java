package utils.api;

import RequestAndResponse.Request;
import RequestAndResponse.Response;

import java.io.IOException;
import java.io.InputStream;

public interface Parser {
    String parseResponse(Response response);
    String parseResponse(InputStream inputStream) throws IOException;
    String parseRequest(Request request);
    String parseRequest(InputStream inputStream);
}
