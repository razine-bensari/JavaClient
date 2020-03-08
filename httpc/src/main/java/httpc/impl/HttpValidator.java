package httpc.impl;

import httpc.api.Validator;
import org.apache.commons.lang3.StringUtils;
import utils.impl.ParsingException;

public class HttpValidator implements Validator {

    @Override
    public void validatePostRequest(String body, String fileBody, String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, String urlfromCLI) {
        if(!StringUtils.isEmpty(body) && !StringUtils.isEmpty(fileBody)) {
            throw new ParsingException("-d and -f cannot be used at the same time. Please choose a single inline-data source for your request.");
        }
    }

    @Override
    public void validateGetRequest(String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, String urlfromCLI) {
        //TODO for further implementation
    }
}
