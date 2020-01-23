/*
  Author: Razine Ahmed Bensari
  COMP445 –Winter 2020
  Data Communications & Computer Networks
  Lab Assignment # 1
  Due Date: Sunday, Feb9, 2020 by 11:59PM
  */
package RequestAndResponse;

import java.io.File;
import java.net.URL;
import java.util.Map;

public final class Request {

    private URL url;
    private Method httpMethod;
    private Map<String, String> headers;
    private Map<String, String> queryParameters;
    private String body;
    private File file;
    private URL redirectUrl;
    private String version;
    private String path;

    private Request(){
        //Private Constructor to force the creation of this object via the builder
    }

    public Request(Builder builder){
        //Ask TA if we can use this class to automatically parse the string into a valid url object
        this.url = builder.url;
        this.httpMethod = builder.httpMethod;
        this.headers = builder.headers;
        this.queryParameters = builder.queryParameters;
        this.body = builder.body;
        this.file = builder.file;
        this.redirectUrl = builder.redirectUrl;
        this.version = builder.version;
        this.path = builder.path;
    }

    public static class Builder {

        private URL url;
        private Method httpMethod;
        private Map<String, String> headers;
        private Map<String, String> queryParameters;
        private String body;
        private File file;
        private URL redirectUrl;
        private String version;
        private String path;

        //Private Constructor
        public Builder(){

        }

        //No need for factory pattern
        public Builder(String url){ //May need to use singleton class if this constructor instantiation does not work. Implies transfer this into a method
            try {
                this.url = new URL(url);
                this.path = this.url.getPath();
            }catch (Exception e){
                System.out.println("A Java exception has occured while trying to parse the url");
                System.out.println("Please, input a valid url" + e.getMessage() + " ");
                System.exit(1);
            }
        }

        public Request Build(){
            return new Request(this);
        }

        public Builder withHttpMethod(Method httpMethod){
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder withRedirectUrl(URL url){
            this.redirectUrl = url;
            return this;
        }

        public Builder withPath(String path){
            this.path = path;
            return this;
        }

        public Builder withHeaders(Map<String, String> headers){
            this.headers = headers;
            return this;
        }

        public Builder withQueryParameters(Map<String, String> queryParameters){
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder withBody(String body){
            this.body = body;
            return this;
        }

        public Builder withFile(File file){
            this.file = file;
            return this;
        }

        public Builder withUrl(URL url){
            this.url = url;
            return this;
        }

        public Builder withVersion(String version){
            this.version = version;
            return this;
        }
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Method getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(Method httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public URL getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(URL redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setVersion(String version) { this.version = version; }

    public String getVersion() { return version; }

    public void setPath(String path) { this.path = path; }

    public String getPath() { return path; }
}
