package org.abpass.json;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class JsonPrimitiveValueHandler extends JsonBaseHandler<Object> {
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        complete(o);
        return super.primitive(o);
    }
}
