/*
  Author: Razine Ahmed Bensari
  COMP445 –Winter 2020
  Data Communications & Computer Networks
  Lab Assignment # 1
  Due Date: Sunday, Feb9, 2020 by 11:59PM
  */
package httpc;

import RequestAndResponse.Request;
import RequestAndResponse.Response;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;


@Command(name = "httpc",
        description = "httpc is a curl-like application but supports HTTP protocol only.",
        version = "httpc CLI version 1.0.0")
public class Httpc implements Callable<Integer> {

    public volatile Request request;

    @Option(names = {"-v", "--verbose"}, description = "Shows verbose output.")
    private boolean verbose;

    @Command(name = "get", helpCommand = true, description = "Set the Method type of the HTTP request as GET. Valid values: ${COMPLETION-CANDIDATES}")
    public Response get(
            @Option(names = {"-h", "--headers"}, description = "Associates headers to HTTP Request with the format 'key:value'.") String headersFromCLI,
            @Option(names = {"-o", "--output"}, description = "Outputs the returned response to a file") String fileName,
            @Option(names = {"-q", "--query"}, description = "Appends the query to the associated url.") String queryFromCLI,
            @Option(names = {"-r", "--redirect"}, description = "Associates the request with a Redirect Url") String redirectUrlFromCLI,
            @Parameters(index = "0") String urlfromCLI
    ){
        System.out.println("GET method has been executed");
        return null;//TODO
    }
    @Command(name = "post", helpCommand = true, description = "Set the Method type of the HTTP request as POST.")
    public Response post(
            @Option(names = {"-d", "--data"}, description = "Associates an inline data to the body HTTP POST request.") String body,
            @Option(names = {"-f", "--file"}, description = "Associates the content of a file to the body HTTP POST.") File file,
            @Option(names = {"-h", "--headers"}, description = "Associates headers to HTTP Request with the format 'key:value'.") String headersFromCLI,
            @Option(names = {"-o", "--output"}, description = "Outputs the returned response to a file") String fileName,
            @Option(names = {"-q", "--query"}, description = "Appends the query to the associated url.") String queryFromCLI,
            @Option(names = {"-r", "--redirect"}, description = "Associates the request with a Redirect Url") String redirectUrlFromCLI,
            @Parameters(index = "0") String urlfromCLI
    ){
        System.out.println("POST method has been executed");
        return null;//TODO
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new Httpc()).execute(args));
    }

    public Integer call() {
        System.out.println("httpc has been called");
        return 0; //My error code
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}