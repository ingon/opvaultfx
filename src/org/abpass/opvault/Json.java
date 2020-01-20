package org.abpass.opvault;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.json.zero.ParseException;
import org.json.zero.hl.JsonBaseHandler;
import org.json.zero.hl.JsonTypedHandler;

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

class JsonURLHandler extends JsonBaseHandler<URL> {
    @Override
    public boolean stringValue(char[] source, int begin, int end, int escapeCount) throws ParseException {
        try {
            complete(new URL(readString(source, begin, end, escapeCount)));
        } catch (MalformedURLException e) {
            throw new ParseException(-1, "not an url");
        }
        return true;
    }
}