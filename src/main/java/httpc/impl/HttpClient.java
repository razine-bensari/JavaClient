package httpc.impl;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import httpc.api.Client;
import utils.api.Converter;
import utils.api.Parser;
import utils.impl.HttpParser;
import utils.impl.HttpResponseConverter;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class HttpClient implements Client {

    private Parser parser = new HttpParser();
    private Converter<Response, String> converter = new HttpResponseConverter();

    public Response get(Request request) {
        return getResponse(request);
    }

    public Response post(Request request) {
        return getResponse(request);
    }

    private Response getResponse(Request request) {
        try {
            InetAddress addr = InetAddress.getByName(request.getUrl().getHost());


            Socket socket = new Socket(addr, 80); //support only port 80 as of now

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(parser.parseRequest(request).getBytes()); //Writes the request to the outputstream after being parsed from object to string
            outputStream.flush();

            String response = parser.parseResponse(inputStream); //Returns the parsed response (using http specification).
            //System.out.println(response);

            return converter.convert(response); //Converts parsed response into Response object

        } catch (Exception e) {
            System.out.printf("%s", e.getMessage());
        }
        return new Response();
    }

    public HttpClient(){

    }
    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public Converter<Response, String> getConverter() {
        return converter;
    }

    public void setConverter(Converter<Response, String> converter) {
        this.converter = converter;
    }
}
