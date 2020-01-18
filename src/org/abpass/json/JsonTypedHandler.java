package org.abpass.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.json.simple.parser.ParseException;

public class JsonTypedHandler<T> extends JsonBaseHandler<T> {
    private final Supplier<T> factory;
    
    private final Map<String, JsonBaseHandler<?>> handlers = new HashMap<>();
    
    private T instance;
    
    public JsonTypedHandler(Supplier<T> factory) {
        this.factory = factory;
    }
    
    public void stringProperty(String name, BiConsumer<T, String> consumer) {
        anyProperty(name, new JsonStringHandler(), consumer);
    }
    
    public void numberProperty(String name, BiConsumer<T, Number> consumer) {
        anyProperty(name, new JsonNumberHandler(), consumer);
    }
    
    public void booleanProperty(String name, BiConsumer<T, Boolean> consumer) {
        anyProperty(name, new JsonBooleanHandler(), consumer);
    }
    
    public <S> void objectProperty(String name, JsonTypedHandler<S> handler, BiConsumer<T, S> consumer) {
        anyProperty(name, handler, consumer);
    }
    
    public <S> void arrayProperty(String name, JsonArrayHandler<S> handler, BiConsumer<T, List<S>> consumer) {
        anyProperty(name, handler, consumer);
    }
    
    public void valueProperty(String name, BiConsumer<T, Object> consumer) {
        anyProperty(name, new JsonValueHandler(), consumer);
    }
    
    public void primitiveProperty(String name, BiConsumer<T, Object> consumer) {
        anyProperty(name, new JsonPrimitiveValueHandler(), consumer);
    }
    
    protected <S> void anyProperty(String name, JsonBaseHandler<S> handler, BiConsumer<T, S> consumer) {
        handler.valueConsumer = (s) -> consumer.accept(instance, s);
        handlers.put(name, handler);
    }
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        instance = factory.get();
        return super.startObject();
    }
    
    @Override
    public boolean startObjectEntry(String name) throws ParseException, IOException {
        var handler = handlers.get(name);
        if (handler == null) {
            throw new ParseException(2, String.format("(parser %s) handler not found: %s", instance.getClass(), name));
        }
        parser.enque(name, handler);
        return super.startObjectEntry(name);
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        complete(instance);
        return super.endObject();
    }
}