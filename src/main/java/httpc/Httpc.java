/*
  Author: Razine Ahmed Bensari
  COMP445 –Winter 2020
  Data Communications & Computer Networks
  Lab Assignment # 1
  Due Date: Sunday, Feb9, 2020 by 11:59PM
  */
package httpc;

import RequestAndResponse.Response;
import httpc.api.Executor;
import httpc.api.Validator;
import httpc.impl.HttpExecutor;
import httpc.impl.HttpValidator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import utils.impl.HttpParser;
import utils.impl.HttpRequestConverter;
import utils.impl.HttpResponseConverter;

import java.util.concurrent.Callable;


@Command(name = "httpc",
        commandListHeading = "%nThe commands are:%n",
        subcommands = {CommandLine.HelpCommand.class},
        description = "httpc is a curl-like application but supports HTTP protocol only.",
        version = "httpc CLI version 1.0.0")
public class Httpc implements Callable<Integer> {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public Executor executor = new HttpExecutor();
    private HttpParser parser = new HttpParser();
    private HttpRequestConverter reqConverter = new HttpRequestConverter();
    private HttpResponseConverter resConverter = new HttpResponseConverter();
    private Validator validator = new HttpValidator();

    @Command(name = "get", helpCommand = true, description = "executes a HTTP GET request and prints the response.")
    public Response get(
            @Option(names = {"-h", "--headers"}, description = "Associates headers to HTTP Request with the format 'key:value'.") String[] headers,
            @Option(names = {"-o", "--output"}, description = "Outputs the returned response to a file") String fileName,
            @Option(names = {"-q", "--query"}, description = "Appends the query to the associated url.") String[] query,
            @Option(names = {"-r", "--redirect"}, description = "Associates the request with a Redirect Url") String redirectUrl,
            @Option(names = {"-v", "--verbose"}, description = "Shows verbose output.") boolean verbose,
            @Parameters(index = "0") String url
    ){
        System.out.println("GET method has been executed\n");
        System.out.println(ANSI_GREEN + "----- Response Output ------" + ANSI_RESET);

        /* Exits if not valid */
        validator.validateGetRequest(headers, fileName, query, redirectUrl, url);

        Response response = null;

        try{
            response = executor.executeGET(headers, fileName, query, redirectUrl, url);
            if(verbose){
                System.out.println(parser.parseResponse(response));
            } else {
                System.out.println(response.getBody());
            }
            return response;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return response;
    }
    @Command(name = "post", helpCommand = true, description = "executes a HTTP POST request and prints the response.")
    public Response post(
            @Option(names = {"-d", "--data"}, description = "Associates an inline data to the body HTTP POST request.") String body,
            @Option(names = {"-f", "--file"}, description = "Associates the content of a file to the body HTTP POST.") String fileBody,
            @Option(names = {"-h", "--headers"}, description = "Associates headers to HTTP Request with the format 'key:value'.") String[] headers,
            @Option(names = {"-o", "--output"}, description = "Outputs the returned response to a file") String fileName,
            @Option(names = {"-q", "--query"}, description = "Appends the query to the associated url.") String[] query,
            @Option(names = {"-r", "--redirect"}, description = "Associates the request with a Redirect Url") String redirectUrl,
            @Option(names = {"-v", "--verbose"}, description = "Shows verbose output.") boolean verbose,
            @Parameters(index = "0") String url
    ){
        System.out.println("POST method has been executed\n");
        System.out.println(ANSI_GREEN + "----- Response ------" + ANSI_RESET);

        Response response = null;

        validator.validatePostRequest(body, fileBody, headers, fileName, query, redirectUrl, url);

        try{
            response = executor.executePOST(body, fileBody, headers, fileName, query, redirectUrl, url);
            if(verbose){
                System.out.println(parser.parseResponse(response));
            } else {
                System.out.println(response.getBody());
            }
            return response;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return response;
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new Httpc()).execute(args));
    }

    public Integer call() {
        System.out.println("httpc has been called");
        return 0; //My error code
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public HttpParser getParser() {
        return parser;
    }

    public void setParser(HttpParser parser) {
        this.parser = parser;
    }

    public HttpRequestConverter getReqConverter() {
        return reqConverter;
    }

    public void setReqConverter(HttpRequestConverter reqConverter) {
        this.reqConverter = reqConverter;
    }

    public HttpResponseConverter getResConverter() {
        return resConverter;
    }

    public void setResConverter(HttpResponseConverter resConverter) {
        this.resConverter = resConverter;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}