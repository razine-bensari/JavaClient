package utils.impl;

import httpc.ParsingException;
import org.apache.commons.lang3.StringUtils;
import utils.api.Converter;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaderConverter implements Converter<Map<String, String>, String[]> {
    public Map<String, String> convert(String[] o) {

        Map<String, String> headersMap = new HashMap<String, String>();

        for (String s : o) {
            if(!s.contains(":") || !(StringUtils.countMatches(s, ":") == 1)){
                throw new ParsingException("Invalid header: " + s);
            }
            String[] temp = s.split(":");
            headersMap.put(temp[0], temp[1]);
        }
        return headersMap;
    }
}
