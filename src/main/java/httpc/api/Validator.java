package httpc.api;

public interface Validator {
    void validatePostRequest(String body, String fileBody, String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, String urlfromCLI);
}
