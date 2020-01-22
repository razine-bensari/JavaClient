package httpc.api;

import RequestAndResponse.Response;

public interface Executor {
    Response executePOST(String body, String headersFromCLI, String fileName, String queryFromCLI, String redirectUrlFromCLI, String urlfromCLI);
    Response executeGET(String headersFromCLI, String fileName, String queryFromCLI, String redirectUrlFromCLI, String urlfromCLI);
}
