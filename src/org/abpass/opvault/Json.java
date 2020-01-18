package org.abpass.opvault;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.abpass.json.JsonBaseHandler;
import org.abpass.json.JsonTypedHandler;
import org.json.simple.parser.ParseException;

class Json<T> extends JsonTypedHandler<T> {
    public Json(Supplier<T> factory) {
        super(factory);
    }
    
    public void base64Property(String name, BiConsumer<T, byte[]> consumer) {
        stringProperty(name, (t, o) -> consumer.accept(t, Decrypt.decode(o)));
    }
    
    public void instantProperty(String name, BiConsumer<T, Instant> consumer) {
        numberProperty(name, (t, o) -> consumer.accept(t, Instant.ofEpochSecond(o.longValue())));
    }
    
    public void urlProperty(String name, BiConsumer<T, URL> consumer) {
        anyProperty(name, new JsonURLHandler(), consumer);
    }
}

class ItemUrl {
    static JsonTypedHandler<ItemUrl> newParser() {
        var handler = new Json<ItemUrl>(ItemUrl::new);
        handler.urlProperty("u", (t, o) -> t.url = o);
        return handler;
    }
    
    URL url;
}

class JsonURLHandler extends JsonBaseHandler<URL> {
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        var s = (String) o;
        complete(new URL(s));
        return super.primitive(o);
    }
}