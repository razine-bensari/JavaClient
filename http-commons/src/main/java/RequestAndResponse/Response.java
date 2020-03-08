/*
  Author: Razine Ahmed Bensari
  COMP445 –Winter 2020
  Data Communications & Computer Networks
  Lab Assignment # 1
  Due Date: Sunday, Feb9, 2020 by 11:59PM
  */
package RequestAndResponse;

import java.util.Map;

public class Response {

    private String version;

    private String statusCode;

    private String phrase;

    private Map<String, String> headers;

    private String body;

    public Response() {

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
