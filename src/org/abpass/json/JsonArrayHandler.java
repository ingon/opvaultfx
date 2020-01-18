package org.abpass.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

public class JsonArrayHandler<T> extends JsonBaseHandler<List<T>> {
    private final JsonBaseHandler<T> elementHandler;
    private List<T> list;
    
    public JsonArrayHandler(JsonBaseHandler<T> elementHandler) {
        this.elementHandler = elementHandler;
        this.elementHandler.valueConsumer = (v) -> {
            list.add(v);
            expectNext();
        };
    }
    
    private void expectNext() {
        parser.enque("[]", new ArrayElementHandler());
        elementHandler.parser = parser;
    }
    
    @Override
    public boolean startArray() throws ParseException, IOException {
        list = new ArrayList<T>();
        expectNext();
        return super.startArray();
    }
    
    @Override
    public boolean endArray() throws ParseException, IOException {
        elementHandler.parser = null;
        complete(list);
        return super.endArray();
    }
    
    private class ArrayElementHandler extends JsonBaseHandler<T> {
        @Override
        public boolean primitive(Object o) throws ParseException, IOException {
            return elementHandler.primitive(o);
        }
        
        @Override
        public boolean startObject() throws ParseException, IOException {
            return elementHandler.startObject();
        }
        
        @Override
        public boolean startObjectEntry(String name) throws ParseException, IOException {
            return elementHandler.startObjectEntry(name);
        }
        
        @Override
        public boolean endObject() throws ParseException, IOException {
            return elementHandler.endObject();
        }
        
        @Override
        public boolean startArray() throws ParseException, IOException {
            return elementHandler.startArray();
        }
        
        @Override
        public boolean endArray() throws ParseException, IOException {
            parser.deque();
            return (JsonArrayHandler.this).endArray();
        }
    }
}