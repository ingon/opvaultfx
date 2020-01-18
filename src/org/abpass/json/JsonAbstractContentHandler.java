package org.abpass.json;

import java.io.IOException;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

public class JsonAbstractContentHandler implements ContentHandler {
    // global
    @Override
    public void startJSON() throws ParseException, IOException {
    }

    @Override
    public void endJSON() throws ParseException, IOException {
    }

    // object entry
    @Override
    public boolean startObjectEntry(String name) throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    // primitives
    @Override
    public boolean primitive(Object o) throws ParseException, IOException {
        return true;
    }

    // object
    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        return true;
    }

    // array
    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        return true;
    }
}
