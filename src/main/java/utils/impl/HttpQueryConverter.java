package utils.impl;

import utils.api.Converter;

import java.util.HashMap;
import java.util.Map;

public class HttpQueryConverter implements Converter<Map<String, String>, String> {
    public Map<String, String> convert(String o) {

        Map<String, String> queryMap = new HashMap<String, String>();

        String[] str = o.split(",");
        for (String s : str) {
            String[] temp = s.split("=");
            queryMap.put(temp[0], temp[1]);
        }
        return queryMap;
    }
}
