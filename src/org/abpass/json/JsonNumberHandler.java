package org.abpass.json;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class JsonNumberHandler extends JsonBaseHandler<Number> {
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        complete((Number) o);
        return super.primitive(o);
    }
}