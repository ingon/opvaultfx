package org.abpass.opvault;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.json.zero.ParseException;
import org.json.zero.hl.JsonBaseHandler;
import org.json.zero.hl.JsonTypedHandler;

class Json<T> extends JsonTypedHandler<T> {
    private static DateTimeFormatter MONTH_YEAR_FORMAT_LONG = DateTimeFormatter.ofPattern("yyyyMM");
    private static DateTimeFormatter MONTH_YEAR_FORMAT_SHORT = DateTimeFormatter.ofPattern("uuMM");
    
    public Json(Supplier<T> factory) {
        super(factory);
    }
    
    public void base64Property(String name, BiConsumer<T, byte[]> consumer) {
        anyProperty(name, new JsonBase64Handler(), consumer);
    }
    
    public void instantProperty(String name, BiConsumer<T, Instant> consumer) {
        numberProperty(name, (t, o) -> consumer.accept(t, Instant.ofEpochSecond(o.longValue())));
    }
    
    public void monthYearProperty(String name, BiConsumer<T, YearMonth> consumer) {
        numberProperty(name, (t, o) -> {
            for (var f : Arrays.asList(MONTH_YEAR_FORMAT_LONG, MONTH_YEAR_FORMAT_SHORT)) {
                try {
                    consumer.accept(t, YearMonth.parse(o.toString(), f));
                    return;
                } catch (DateTimeParseException exc) {
                }
            }
            //throw new IllegalArgumentException("unknown format");
        });
    }
    
    public void urlProperty(String name, BiConsumer<T, URL> consumer) {
        anyProperty(name, new JsonURLHandler(), consumer);
    }
    
    public void secureStringProperty(String name, BiConsumer<T, SecureString> consumer) {
        anyProperty(name, new JsonSecureStringHandler(), consumer);
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
        complete(Security.decode(readString(source, begin, end, escapeCount)));
        return true;
    }
}

class JsonSecureStringHandler extends JsonBaseHandler<SecureString> {
    @Override
    public boolean stringValue(char[] source, int begin, int end, int escapeCount) throws ParseException {
        complete(new SecureString(source, begin, end - begin));
        return true;
    }
}
