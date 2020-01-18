package org.abpass.json;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class JsonStringHandler extends JsonBaseHandler<String> {
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        complete((String) o);
        return super.primitive(o);
    }
}