package httpc.impl;

import httpc.api.Validator;
import org.apache.commons.lang3.StringUtils;

public class HttpValidator implements Validator {

    @Override
    public void validatePostRequest(String body, String fileBody, String[] headersFromCLI, String fileName, String[] queryFromCLI, String redirectUrlFromCLI, String urlfromCLI) {
        if(!StringUtils.isEmpty(body) && !StringUtils.isEmpty(fileBody)) {
            System.out.println("-d and -f cannot be used at the same time. Please choose a single inline-data source for your request.");
            System.exit(1);
        }
    }
}
