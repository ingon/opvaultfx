package org.abpass.json;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class JsonValueHandler extends JsonBaseHandler<Object> {
    private JsonBaseHandler<?> delegate;
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        delegate = new JsonMapHandler<Object>(new JsonValueHandler());
        delegate.valueConsumer = (o) -> complete(o);
        parser.enque(".", delegate);
        delegate.startObject();
        return super.startObject();
    }
    
    @Override
    public boolean startArray() throws ParseException, IOException {
        delegate = new JsonArrayHandler<Object>(new JsonValueHandler());
        delegate.valueConsumer = (o) -> complete(o);
        parser.enque("[]", delegate);
        delegate.startArray();
        return super.startArray();
    }
    
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        complete(o);
        return super.primitive(o);
    }
}
