package org.abpass.json;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class JsonBooleanHandler extends JsonBaseHandler<Boolean> {
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        complete((Boolean) o);
        return super.primitive(o);
    }
}