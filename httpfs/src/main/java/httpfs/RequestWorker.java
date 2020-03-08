package httpfs;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import utils.api.Converter;
import utils.api.Parser;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestWorker implements Runnable {

    private Socket clientSocket;
    private Parser parser = new HttpParser();
    private Converter<Request, String> converter = new HttpRequestConverter();

    public RequestWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        try{
            //InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            String body = "Hello There";
            String response = "HTTP/1.0 200 OK\r\nContent-Type:text/html\r\nContent-Length:" + body.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + body;
            System.out.println("Received request, here is the response: \n" + response);
            out.write(response.getBytes());
            out.flush();
        }catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public Response processRequest(Request request) {

        Response response = new Response();
        response.setStatusCode("200");
        response.setBody("{here is your file}");
        response.setPhrase("OK");
        response.setVersion("HTTP/1.0");

        return response;
    }
}
