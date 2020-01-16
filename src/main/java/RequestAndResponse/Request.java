package RequestAndResponse;

import picocli.CommandLine.Option

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class Request {

    private Request(){
        //Private Constructor to force the creation of this object via the builder
    }

    public Request(Builder builder){
        //Ask TA if we can use this class to automatically parse the string into a valid url object
        URL url = builder.url;
        Method httpMethod = builder.httpMethod;
        Map<String, String> headers = builder.headers;
        Map<String, String> queryParameters = builder.queryParameters;
        String body = builder.body;
        File file = builder.file;
        String redirectUrl = builder.redirectUrl;
    }

    public static class Builder {

        private URL url;
        private Method httpMethod;

        @Option(names = {"-h", "--headers"}, description = "Associates headers to HTTP Request with the format 'key:value'.")
        private Map<String, String> headers;

        @Option(names = {"-q", "--query"}, description = "Appends the query to the associated url.")
        private Map<String, String> queryParameters;

        @Option(names = {"-d", "--data"}, description = "Associates an inline data to the body HTTP POST request.")
        private String body;

        @Option(names = {"-f", "--file"}, description = "Associates the content of a file to the body HTTP POST.")
        private File file;
        private String redirectUrl;

        //Private Constructor
        private Builder(){

        }

        //No need for factory pattern
        public Builder(String url){ //May need to use singleton class if this constructor instantiation does not work. Implies transfer this into a method
            try {
                this.url = new URL(url);
            }catch (Exception e){
                System.out.println("A Java exception has occured while trying to parse the url");
                System.out.println("Please, input a valid url" + e.getMessage() + " ");
            }
        }

        public Request Build(){
            return new Request(this);
        }

        public Builder withHttpMethod(Method httpMethod){
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder withRedirectUrl(String url){
            this.redirectUrl = url;
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
    }
}
