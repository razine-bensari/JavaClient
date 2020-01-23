package utils.impl;

import httpc.ParsingException;
import org.apache.commons.lang3.StringUtils;
import utils.api.Converter;

import java.util.HashMap;
import java.util.Map;

public class HttpQueryConverter implements Converter<Map<String, String>, String[]> {
    public Map<String, String> convert(String[] o) {

        Map<String, String> queryMap = new HashMap<String, String>();

        for (String s : o) {
            if(!s.contains("=") || !(StringUtils.countMatches(s, "=") == 1)){
                throw new ParsingException("Invalid query" + s);
            }
            String[] temp = s.split("=");
            queryMap.put(temp[0], temp[1]);
        }
        return queryMap;
    }
}
