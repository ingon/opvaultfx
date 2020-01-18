package org.abpass.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;

public class JsonMapHandler<V> extends JsonBaseHandler<Map<String, V>> {
    private final JsonBaseHandler<V> elementHandler;
    private Map<String, V> map;

    public JsonMapHandler(JsonBaseHandler<V> elementHandler) {
        this.elementHandler = elementHandler;
    }
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        map = new HashMap<String, V>();
        return super.startObject();
    }
    
    @Override
    public boolean startObjectEntry(String name) throws ParseException, IOException {
        elementHandler.valueConsumer = (v) -> map.put(name, v);
        parser.enque(name, elementHandler);
        return super.startObjectEntry(name);
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        complete(map);
        return super.endObject();
    }
}
