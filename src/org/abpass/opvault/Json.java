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
        anyProperty(name, new JsonBase64Handler(), consumer);
    }
    
    public void instantProperty(String name, BiConsumer<T, Instant> consumer) {
        numberProperty(name, (t, o) -> consumer.accept(t, Instant.ofEpochSecond(o.longValue())));
    }
    
    public void urlProperty(String name, BiConsumer<T, URL> consumer) {
        anyProperty(name, new JsonURLHandler(), consumer);
    }
    
    public void secureStringProperty(String name, BiConsumer<T, SecureString> consumer) {
        anyProperty(name, new JsonFieldValueHandler(), consumer);
    }
    
    public void sectionFieldProperty(String name, BiConsumer<T, Object> consumer) {
        anyProperty(name, new JsonSectionFieldValueHandler(), consumer);
    }
}

class JsonURLHandler extends JsonBaseHandler<URL> {
    @Override
    public boolean stringValue(char[] source, int begin, int end, int escapeCount) throws ParseException {
        try {
            complete(new URL(readString(source, begin, end, escapeCount)));
        } catch (MalformedURLException e) {
            throw new ParseException(-1, "not an url: " + e.getMessage());
        }
        return true;
    }
}

class JsonBase64Handler extends JsonBaseHandler<byte[]> {
    @Override
    public boolean stringValue(char[] source, int begin, int end, int escapeCount) throws ParseException {
        complete(Decrypt.decode(readString(source, begin, end, escapeCount)));
        return true;
    }
}

class JsonFieldValueHandler extends JsonBaseHandler<SecureString> {
    @Override
    public boolean stringValue(char[] source, int begin, int end, int escapeCount) throws ParseException {
        complete(new SecureString(source, begin, end - begin));
        return true;
    }
}

class JsonSectionFieldValueHandler extends JsonBaseHandler<Object> {
    @Override
    public boolean stringValue(char[] source, int begin, int end, int escapeCount) throws ParseException {
        complete(new SecureString(source, begin, end - begin));
        return true;
    }
    
    @Override
    public boolean longValue(char[] source, int begin, int end) throws ParseException {
        complete(readLong(source, begin, end));
        return true;
    }
    
    @Override
    public boolean doubleValue(char[] source, int begin, int end) throws ParseException {
        complete(readDouble(source, begin, end));
        return true;
    }
}