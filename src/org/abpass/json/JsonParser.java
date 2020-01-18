package org.abpass.json;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser<V> extends JsonAbstractContentHandler {
    public static <T> T parse(byte[] data, JsonBaseHandler<T> handler) throws ParseException {
        return parse(new String(data), handler);
    }
    
    public static <T> T parse(String data, JsonBaseHandler<T> handler) throws ParseException {
//        System.out.println("PARSE: " + data);
        var parser = new JsonParser<T>(handler);
        try {
            new JSONParser().parse(data, parser);
            return parser.getValue();
        } catch (ParseException e) {
            System.out.format("error in path (%s): %s\n", parser.debug, e);
            throw e;
        }
    }
    
    private final JsonBaseHandler<V> root;
    private final Deque<String> debug = new LinkedList<String>();
    private final Deque<JsonBaseHandler<?>> stack = new LinkedList<JsonBaseHandler<?>>();
    private V value;
    
    public JsonParser(JsonBaseHandler<V> root) {
        this.root = root;
    }
    
    public V getValue() {
        return value;
    }
    
    @Override
    public void startJSON() throws ParseException, IOException {
        root.valueConsumer = (v) -> {
            value = v;
        };
        enque("/", root);
    }
    
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        return stack.peekLast().primitive(o);
    }
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        return stack.peekLast().startObject();
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        return stack.peekLast().endObject();
    }
    
    @Override
    public boolean startObjectEntry(String name) throws ParseException, IOException {
        return stack.peekLast().startObjectEntry(name);
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return stack.peekLast().endObjectEntry();
    }
    
    @Override
    public boolean startArray() throws ParseException, IOException {
        return stack.peekLast().startArray();
    }
    
    @Override
    public boolean endArray() throws ParseException, IOException {
        return stack.peekLast().endArray();
    }
    
    protected void enque(String name, JsonBaseHandler<?> handler) {
        handler.setParser(this);
        debug.offerLast(name);
        stack.offerLast(handler);
    }
    
    protected void deque() {
        debug.pollLast();
        stack.pollLast();
    }
}
