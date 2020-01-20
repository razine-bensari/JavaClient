package utils.api;

import RequestAndResponse.Request;

import java.io.InputStream;

public interface Parser {
    String parseResponse(InputStream inputStream);
    String parseRequest(Request request);
}
