package org.abpass.json;

import java.util.function.Consumer;

public class JsonBaseHandler<T> extends JsonAbstractContentHandler {
    JsonParser<?> parser;
    Consumer<T> valueConsumer = (t) -> {};
    
    protected void setParser(JsonParser<?> parser) {
        this.parser = parser;
    }
    
    protected void complete(T value) {
        parser.deque();
        valueConsumer.accept(value);
    }
}