package httpc.api;

import RequestAndResponse.Response;

import java.io.File;

public interface Executor {
    Response executePOST(String body, File file, String headersFromCLI, String fileName, String queryFromCLI, String redirectUrlFromCLI, String urlfromCLI);
    Response executeGET(String headersFromCLI, String fileName, String queryFromCLI, String redirectUrlFromCLI, String urlfromCLI);
}
