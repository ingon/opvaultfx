package org.abpass.opvault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Json {
    public static Map<String, Object> parse(byte[] data) {
        return parse(new String(data));
    }
    
    public static Map<String, Object> parse(String data) {
        try {
        return (Map<String, Object>) new JSONParser().parse(data, new ContainerFactory() {
            @Override
            public Map createObjectContainer() {
                return new HashMap();
            }
            
            @Override
            public List creatArrayContainer() {
                return new ArrayList();
            }
        });
        } catch(ParseException exc) {
            throw new RuntimeException(exc);
        }
    }
}
