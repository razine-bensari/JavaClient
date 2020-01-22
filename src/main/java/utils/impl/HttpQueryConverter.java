package utils.impl;

import utils.api.Converter;

import java.util.HashMap;
import java.util.Map;

//Converts a string query into
public class HttpQueryConverter implements Converter<Map<String, String>, String> {
    public Map<String, String> convert(String o) {

        Map<String, String> queryMap = new HashMap<String, String>();

        String[] str = o.split("&");
        if (str.length == 1) {
            String[] temp = str[0].split("=");
            for(int i=0; i < temp.length; i+=2){
                queryMap.put(temp[i], temp[i+1]);
            }
            return queryMap;
        } else {
            for (String s : str) {
                String[] temp = s.split("=");
                queryMap.put(temp[0], temp[1]);
            }
            return queryMap;
        }
    }
}
