package httpc.subcommands;

import RequestAndResponse.Response;
import httpc.Colors;
import httpc.api.Executor;
import httpc.api.Validator;
import httpc.impl.HttpExecutor;
import httpc.impl.HttpValidator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import utils.impl.HttpParser;
import utils.impl.HttpResponseConverter;

import java.util.concurrent.Callable;

@Command(name = "post", helpCommand = true, description = "executes a HTTP POST request and prints the response.")
public class Post implements Callable<Response> {

    public Executor executor = new HttpExecutor();
    private Validator validator = new HttpValidator();
    private HttpParser parser = new HttpParser();
    private HttpResponseConverter resConverter = new HttpResponseConverter();

    @Option(names = {"-v", "--verbose"}, description = "Prints the detail of the response such as protocol, status")
    Boolean verbose;

    @Option(names = {"-d", "--data"}, description = "Associates an inline data to the body HTTP POST request.")
    String body;

    @Option(names = {"-f", "--file"}, description = "Associates the content of a file to the body HTTP POST.")
    String fileBody;

    @Option(names = {"-h", "--headers"}, description = "Associates headers to HTTP Request with the format 'key:value'.")
    String[] headers;

    @Option(names = {"-o", "--output"}, description = "Outputs the returned response to a file")
    String fileName;

    @Option(names = {"-q", "--query"}, description = "Appends the query to the associated url.")
    String[] query;

    @Option(names = {"-r", "--redirect"}, description = "Associates the request with a Redirect Url")
    String redirectUrl;

    @Parameters(index = "0")
    String url;


    @Override
    public Response call() {

        System.out.println("POST command has been executed\n");
        System.out.println(Colors.ANSI_GREEN + "----- Response ------" + Colors.ANSI_RESET);

        /* Exits if not valid */
        validator.validatePostRequest(body, fileBody, headers, fileName, query, redirectUrl, url);

        Response response = null;

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
}
